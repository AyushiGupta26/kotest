package io.kotlintest.runner.junit5

import io.kotlintest.Spec
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Modifier
import java.net.URI
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

object TestDiscovery {

  data class DiscoveryRequest(val uris: List<URI>)

  val isSpec: (Class<*>) -> Boolean = { Spec::class.java.isAssignableFrom(it) && !Modifier.isAbstract(it.modifiers) }

  private fun reflections(request: DiscoveryRequest): Reflections {
    println("Will scan ${request.uris.joinToString(":")}")
    return Reflections(ConfigurationBuilder()
        .addUrls(request.uris.map { it.toURL() })
        .setScanners(SubTypesScanner()))
  }

  // returns all the locatable specs for the given request
  private fun scan(request: DiscoveryRequest): List<KClass<out Spec>> =
      reflections(request)
          .getSubTypesOf(Spec::class.java)
          .map(Class<out Spec>::kotlin)
          // must filter out abstract to avoid the spec parent classes themselves
          .filter { !it.isAbstract }

  operator fun invoke(request: DiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
    val root = RootTestDescriptor(uniqueId.append("root", "kotlintest"))
    val specs = scan(request)

    println("Found ${specs.size} specs")
    specs.forEach {
      val spec: Spec = it.createInstance()
      root.addChild(TestContainerDescriptor.fromTestContainer(root.uniqueId, spec.root()))
    }

    return root
  }
}