package io.kotlintest.specs

import io.kotlintest.AbstractSpec
import io.kotlintest.TestCase
import io.kotlintest.TestContainer
import io.kotlintest.TestContext
import io.kotlintest.lineNumber

@Suppress("FunctionName")
abstract class AbstractBehaviorSpec(body: AbstractBehaviorSpec.() -> Unit = {}) : AbstractSpec() {

  init {
    body()
  }

  final override fun isInstancePerTest(): Boolean = false

  fun Given(desc: String, init: GivenContext.() -> Unit) = given(desc, init)
  fun given(desc: String, init: GivenContext.() -> Unit) {
    val name = "Given $desc"
    addRootScope(TestContainer(rootDescription().append(name), this@AbstractBehaviorSpec::class, { GivenContext(it).init() }))
  }

  inner class GivenContext(val context: TestContext) {

    fun and(desc: String, init: GivenContext.() -> Unit) {
      val name = "And $desc"
      context.executeScope(TestContainer(context.currentScope().description().append(name), this@AbstractBehaviorSpec::class, { GivenContext(it).init() }))
    }

    fun When(desc: String, init: WhenContext.() -> Unit) = `when`(desc, init)
    fun `when`(desc: String, init: WhenContext.() -> Unit) {
      val name = "When $desc"
      context.executeScope(TestContainer(context.currentScope().description().append(name), this@AbstractBehaviorSpec::class, { WhenContext(it).init() }))
    }
  }

  inner class WhenContext(val context: TestContext) {

    fun and(desc: String, init: WhenContext.() -> Unit) {
      val name = "And $desc"
      context.executeScope(TestContainer(rootDescription().append(name), this@AbstractBehaviorSpec::class, { WhenContext(it).init() }))
    }

    fun Then(desc: String, test: TestContext.() -> Unit): TestCase = then(desc, test)
    fun then(desc: String, test: TestContext.() -> Unit): TestCase {
      val name = "Then $desc"
      val tc = TestCase(context.currentScope().description().append(name), this@AbstractBehaviorSpec, test, lineNumber(), defaultTestCaseConfig)
      context.executeScope(tc)
      return tc
    }
  }
}
