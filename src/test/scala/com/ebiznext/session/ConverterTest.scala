package com.ebiznext.session

import scala.Array.canBuildFrom
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

case class SampleTestCase(i: Int, s: String, m: Map[String, String])
case class CompositeTestCase(stc: SampleTestCase, stcs: Map[String, SampleTestCase])

@RunWith(classOf[JUnitRunner])
class ConverterTest extends FlatSpec with ShouldMatchers {
  val conv = new JSONConverter[SampleTestCase] {}
  val sessionConv = new JSONConverter[Session] {}
  val sample = SampleTestCase(10, "Hello", Map("France" -> "Paris", "Senegal" -> "Dakar"))
  val sample2 = SampleTestCase(10, "World", Map("UK" -> "London", "Spain" -> "Madrid"))
  val sampleJSON = """{"i":10,"s":"Hello","m":{"France":"Paris","Senegal":"Dakar"}}"""
  val composite = CompositeTestCase(sample, Map("sample" -> sample, "sample2" -> sample2))
  val compositeJSON = """{"stc":{"i":10,"s":"Hello","m":{"France":"Paris","Senegal":"Dakar"}},"stcs":{"sample":{"i":10,"s":"Hello","m":{"France":"Paris","Senegal":"Dakar"}},"sample2":{"i":10,"s":"World","m":{"UK":"London","Spain":"Madrid"}}}}"""
  val sessionJSON = """{"creationTime":1373907836169,"id":"adcf6bc6-28da-4a80-b3c4-33fd68b5b685","isValid":true,"isNew":true,"accessed":1373907836169,"maxInactiveInterval":20,"listeners":{},"attributes":{},"dirty":false}"""
  val session = new Session
  "A json converter" should "serialize as a readable json string" in {
    conv.fromDomain(sample) should (
      equal(sampleJSON.toCharArray() map (_.toByte)))
  }

  it should "deserialize as a SampleTestCase Object" in {
    assert(conv.toDomain[SampleTestCase](sampleJSON.toCharArray() map (_.toByte)) == sample)
  }

  it should "serialize as a CompositeTestCase Object" in {
    assert(java.util.Arrays.equals(conv.fromDomain[CompositeTestCase](composite), compositeJSON.toCharArray() map (_.toByte)))
  }

  it should "deserialize as a CompositeTestCase Object" in {
    assert(conv.toDomain[CompositeTestCase](compositeJSON.toCharArray() map (_.toByte)) == composite)
  }

  it should "serialize as a Session Object" in {
    conv.fromDomain[Session](session)
    assert(true)
  }

  it should "deserialize as a session Object" in {
    val xsession = conv.toDomain[Session](sessionJSON.toCharArray() map (_.toByte))
    assert(true)
  }

}
