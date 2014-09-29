package com.dreweaster.octodubstep.core

import java.util

import com.dreweaster.octodubstep.core.source.PropertySource
import com.google.common.base.Optional
import org.mockito.Matchers._
import org.mockito.Mockito
import org.scalatest._
import org.scalatest.mock.MockitoSugar

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
  */
//@RunWith(classOf[JUnitRunner])
class PropertySourceConfigPropertyManagerSpec extends FlatSpec with GivenWhenThen with BeforeAndAfter with Matchers with MockitoSugar {

  var propertySource: PropertySource = _

  var alternativePropertySource: PropertySource = _

  val defaultConverters = DefaultPropertySourceConfigPropertyManager.defaultConverters()

  def aPropertySource = new PropertySourceMocker(propertySource)

  def anAlternativePropertySource = new PropertySourceMocker(alternativePropertySource)

  private def currentValueOf[T](propertyValue: ConfigPropertyValue[T]) = propertyValue.currentValue.get

  private def metadataNameOf[S](propertyValue: ConfigPropertyValue[S]) = propertyValue.metadata.name

  private def metadataTypeOf[S](propertyValue: ConfigPropertyValue[S]) = propertyValue.metadata.`type`

  private def metadataRequiredOf[S](propertyValue: ConfigPropertyValue[S]) = propertyValue.metadata.required

  private def metadataDynamicOf[S](propertyValue: ConfigPropertyValue[S]) = propertyValue.metadata.dynamic

  private def metadataDefaultValueOf[S](propertyValue: ConfigPropertyValue[S]) = propertyValue.metadata.defaultValue

  private def metadataLoadedFromDefaultValueOf[S](propertyValue: ConfigPropertyValue[S]) = propertyValue.metadata.loadedFromDefaultValue

  private def metadataSourceDescriptionOf[S](propertyValue: ConfigPropertyValue[S]) = propertyValue.metadata.source.get.getDescription

  before {
    propertySource = mock[PropertySource]
    alternativePropertySource = mock[PropertySource]
  }

  it should "support fetching a series of variously typed properties from a given config provider" in {
    Given("a property source containing a series of variously typed properties")
    aPropertySource
      .containing("test.string.property" -> "testValue")
      .containing("test.integer.property" -> "100")
      .containing("test.long.property" -> "2000000")
      .containing("test.boolean.property" -> "true")
      .containing("test.list.string.property" -> "the,quick,brown,fox")
      .containing("test.list.integer.property" -> "1,2,3,4")
      .containing("test.list.long.property" -> "1,2,3,4")
      .containing("test.list.boolean.property" -> "true,false,false,true")

    And("a config provider referencing those properties")
    val configProviderClass = classOf[ConfigProviderWithVariousProperties]

    When("attempting to fetch the properties from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(classOf[ConfigProviderWithVariousProperties])

    Then("the properties should be returned correctly")
    currentValueOf(configProvider.testStringProperty) should equal("testValue")
    currentValueOf(configProvider.testIntegerProperty) should equal(100)
    currentValueOf(configProvider.testLongProperty) should equal(2000000)
    currentValueOf(configProvider.testBooleanProperty) should equal(true)
    currentValueOf(configProvider.testStringListProperty) should equal(util.Arrays.asList("the", "quick", "brown", "fox"))
    currentValueOf(configProvider.testIntegerListProperty) should equal(util.Arrays.asList(1, 2, 3, 4))
    currentValueOf(configProvider.testLongListProperty) should equal(util.Arrays.asList(1L, 2L, 3L, 4L))
    currentValueOf(configProvider.testBooleanListProperty) should equal(util.Arrays.asList(true, false, false, true))
  }

  it should "support fetching properties from multiple config providers" in {
    Given("a property source containing some properties")
    aPropertySource
      .containing("test.string.property" -> "testValue")
      .containing("test.integer.property" -> "10")

    And("a two config providers each referencing one of those properties")
    val configProviderClass1 = classOf[ConfigProviderWithString]
    val configProviderClass2 = classOf[ConfigProviderWithInteger]

    When("attempting to fetch the properties from the providers")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass1, configProviderClass2), List(propertySource), defaultConverters)
    val configProvider1 = manager.getConfigProvider(classOf[ConfigProviderWithString])
    val configProvider2 = manager.getConfigProvider(classOf[ConfigProviderWithInteger])

    Then("the properties should be returned correctly")
    currentValueOf(configProvider1.testStringProperty) should equal("testValue")
    currentValueOf(configProvider2.testIntegerProperty) should equal(10)
  }

  it should "load property from the first instance encountered when more than one property source defines the property" in {
    Given("a property source containing a string property")
    aPropertySource
      .containing("test.string.property" -> "testValueFromPropertySource1")
      .withDescription("Property Source 1")

    And("another property source containing a different value for the same property")
    anAlternativePropertySource
      .containing("test.string.property" -> "testValueFromPropertySource2")
      .withDescription("Property Source 2")

    And("a a config provider referencing the property")
    val configProviderClass = classOf[ConfigProviderWithString]

    When("attempting to fetch the property from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource, alternativePropertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(classOf[ConfigProviderWithString])

    Then("the property should be returned correctly")
    currentValueOf(configProvider.testStringProperty) should equal("testValueFromPropertySource1")

    And("the property metadata should return the correct property source")
    metadataSourceDescriptionOf(configProvider.testStringProperty) should equal("Property Source 1")
  }

  it should "load list property from the first instance encountered when more than one property source defines the property" in {
    Given("a property source containing a string property")
    aPropertySource
      .containing("test.list.string.property" -> "property,source,1")
      .withDescription("Property Source 1")

    And("another property source containing a different value for the same property")
    anAlternativePropertySource
      .containing("test.list.string.property" -> "property,source,2")
      .withDescription("Property Source 2")

    And("a a config provider referencing the property")
    val configProviderClass = classOf[ConfigProviderWithStringList]

    When("attempting to fetch the property from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource, alternativePropertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(classOf[ConfigProviderWithStringList])

    Then("the property should be returned correctly")
    currentValueOf(configProvider.testStringListProperty) should be (util.Arrays.asList("property","source", "1"))

    And("the property metadata should return the correct property source")
    metadataSourceDescriptionOf(configProvider.testStringListProperty) should equal("Property Source 1")
  }

  it should "return default value if property is not required and not found in property sources" in {
    Given("a property source containing no properties")
    aPropertySource.containingNoProperties

    And("a config provider referencing a non-required property with a default value")
    val configProviderClass = classOf[ConfigProviderWithNonRequiredString]

    When("attempting to fetch the property from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(configProviderClass)

    Then("the property value should be the expected default value")
    currentValueOf(configProvider.testStringProperty) should be ("default string")

    And("the property metadata should show it was loaded from default value")
    metadataLoadedFromDefaultValueOf(configProvider.testStringProperty) should be(true)
  }

  it should "return default value if list property is not required and not found in property sources" in {
    Given("a property source containing no properties")
    aPropertySource.containingNoProperties

    And("a config provider referencing a non-required list property with a default value")
    val configProviderClass = classOf[ConfigProviderWithStringList]

    When("attempting to fetch the property from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(configProviderClass)

    Then("the property value should be the expected default value")
    currentValueOf(configProvider.testStringListProperty) should be (util.Arrays.asList("four","five", "six"))

    And("the property metadata should show it was loaded from default value")
    metadataLoadedFromDefaultValueOf(configProvider.testStringListProperty) should be(true)
  }

  // ================== METADATA TESTS ==================

  it should "return correct property name in metadata for a given property" in {
    Given("a property source containing a string property")
    aPropertySource
      .containing("test.string.property" -> "testValue")
      .containing("test.list.string.property" -> "one,two,three")

    And("a two config providers each referencing one of those properties")
    val configProviderClass1 = classOf[ConfigProviderWithString]
    val configProviderClass2 = classOf[ConfigProviderWithStringList]

    When("attempting to fetch the property metadata from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass1, configProviderClass2), List(propertySource), defaultConverters)
    val configProvider1 = manager.getConfigProvider(classOf[ConfigProviderWithString])
    val configProvider2 = manager.getConfigProvider(classOf[ConfigProviderWithStringList])

    Then("the property metadata name should be returned correctly")
    metadataNameOf(configProvider1.testStringProperty) should equal("test.string.property")
    metadataNameOf(configProvider2.testStringListProperty) should equal("test.list.string.property")
  }

  it should "return correct property type in metadata for a given basic property" in {
    Given("a property source containing a string property")
    aPropertySource.containing("test.string.property" -> "testValue")

    And("a config provider referencing that property")
    val configProviderClass = classOf[ConfigProviderWithString]

    When("attempting to fetch the property metadata from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(classOf[ConfigProviderWithString])

    Then("the property metadata type should be returned correctly")
    metadataTypeOf(configProvider.testStringProperty) should be("java.lang.String")
    metadataDynamicOf(configProvider.testStringProperty) should be(false)
  }

  it should "return correct property type in metadata for a given list property" in {
    Given("a property source containing a list of strings property")
    aPropertySource.containing("test.list.string.property" -> "the,quick,brown,fox")

    And("a config provider referencing that property")
    val configProviderClass = classOf[ConfigProviderWithStringList]

    When("attempting to fetch the property metadata from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(classOf[ConfigProviderWithStringList])

    Then("the property metadata type should be returned correctly")
    metadataTypeOf(configProvider.testStringListProperty) should equal("java.util.List<java.lang.String>")
  }

  it should "return correct property required state in metadata for a given property" in {
    Given("a property source containing properties with different required states")
    aPropertySource
      .containing("test.string.property" -> "testValue")
      .containing("test.list.string.property" -> "one,two,three")

    And("two config providers each referencing one of those properties")
    val configProviderClass1 = classOf[ConfigProviderWithString]
    val configProviderClass2 = classOf[ConfigProviderWithStringList]

    When("attempting to fetch the properties from the providers")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass1, configProviderClass2), List(propertySource), defaultConverters)
    val configProvider1 = manager.getConfigProvider(configProviderClass1)
    val configProvider2 = manager.getConfigProvider(configProviderClass2)

    Then("the property metadata for required state should be returned correctly")
    metadataRequiredOf(configProvider1.testStringProperty) should be(true)
    metadataRequiredOf(configProvider2.testStringListProperty) should be(false)
  }

  it should "return correct property dynamic state in metadata for a given property" in {
    Given("a property source containing a string property")
    aPropertySource.containing("test.string.property" -> "testValue")

    And("a config provider referencing that property as dynamic")
    val configProviderClass = classOf[ConfigProviderWithDynamicConfigProperty]

    When("attempting to fetch the property metadata from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(classOf[ConfigProviderWithDynamicConfigProperty])

    Then("the property metadata type should be returned correctly")
    metadataTypeOf(configProvider.testStringProperty) should be("java.lang.String")
    metadataDynamicOf(configProvider.testStringProperty) should be(true)
  }

  it should "return correct property default value in metadata for a given property" in {
    Given("a property source containing a string property")
    aPropertySource.containing("test.string.property" -> "testValue")

    And("a config provider referencing that property")
    val configProviderClass = classOf[ConfigProviderWithString]

    When("attempting to fetch the property metadata from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(configProviderClass)

    Then("the property metadata default value should be returned correctly")
    metadataDefaultValueOf(configProvider.testStringProperty) should equal("testDefaultValue")
  }

  it should "return correct property default value in metadata for a given list property" in {
    Given("a property source containing a string property")
    aPropertySource.containing("test.list.string.property" -> "one,two,three")

    And("a config provider referencing that property")
    val configProviderClass = classOf[ConfigProviderWithStringList]

    When("attempting to fetch the property metadata from the provider")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass), List(propertySource), defaultConverters)
    val configProvider = manager.getConfigProvider(configProviderClass)

    Then("the property metadata default value should be returned correctly")
    metadataDefaultValueOf(configProvider.testStringListProperty) should equal("four,five,six")
  }

  it should "return correct property source in metadata for a given property" in {
    Given("two property sources containing some properties")
    aPropertySource.containing("test.string.property" -> "testValue").withDescription("Property source 1")
    anAlternativePropertySource.containing("test.list.string.property" -> "one,two,three").withDescription("Property source 2")

    And("a two config providers each referencing one of those properties")
    val configProviderClass1 = classOf[ConfigProviderWithString]
    val configProviderClass2 = classOf[ConfigProviderWithStringList]

    When("attempting to fetch the properties from the providers")
    val manager = new PropertySourceConfigPropertyManager(List(configProviderClass1, configProviderClass2), List(propertySource, alternativePropertySource), defaultConverters)
    val configProvider1 = manager.getConfigProvider(configProviderClass1)
    val configProvider2 = manager.getConfigProvider(configProviderClass2)

    Then("the property metadata property sources should be returned correctly")
    metadataSourceDescriptionOf(configProvider1.testStringProperty) should equal("Property source 1")
    metadataLoadedFromDefaultValueOf(configProvider1.testStringProperty) should be(false)
    metadataSourceDescriptionOf(configProvider2.testStringListProperty) should equal("Property source 2")
    metadataLoadedFromDefaultValueOf(configProvider2.testStringListProperty) should be(false)
  }

  // FAILURE CASES

  it should "fail if property is required and not found in property sources" in {
  }

  it should "fail if list property is required and not found in property sources" in {

  }

  it should "fail if property is not required, has no default value and is not found in property sources" in {

  }

  it should "fail if list property is not required, has no default value and is not found in property sources" in {

  }

  it should "fail if a property value can't be converted" in (pending)

  it should "fail if a list property value can't be converted" in (pending)

  it should "fail if a property default value can't be converted" in (pending)

  it should "fail if a list property default value can't be converted" in (pending)

  it should "fail if a property is required but is not found in property sources" in (pending)

  // RELOAD CASES

  it should "not overwrite existing non-reloadable properties following reload" in (pending)

  it should "overwrite reloadable property from default value if property no longer defined in property source following reload" in (pending)

  it should "overwrite reloadable properties from same property source following reload" in (pending)

  it should "overwrite reloadable properties from different property source following reload" in (pending)

  // Should be unique instance of a ConfigPropertyValue where it's used in multiple places in the code

  // Interesting case - what happens if property previously mapped to source A, can no longer be mapped to source A but could be mapped to source B?

  // We should synchronize reloading, can't happen in parallel. And only reload one source at a time

  // If reloading a property would lead to some kind of failure, then it won't be reloaded

  it should "fail if no value is available for a reloadable property following reload" in (pending)
}

trait ConfigProviderWithString {
  @ConfigProperty(name = "test.string.property", defaultValue = "testDefaultValue")
  def testStringProperty: ConfigPropertyValue[String]
}

trait ConfigProviderWithNonRequiredString {
  @ConfigProperty(name = "test.string.property", defaultValue = "default string", required = false)
  def testStringProperty: ConfigPropertyValue[String]
}

trait ConfigProviderWithInteger {
  @ConfigProperty(name = "test.integer.property")
  def testIntegerProperty: ConfigPropertyValue[Integer]
}

trait ConfigProviderWithStringList {
  @ConfigProperty(name = "test.list.string.property", required = false, defaultValue = "four,five,six")
  def testStringListProperty: ConfigPropertyValue[java.util.List[String]]
}

trait ConfigProviderWithDynamicConfigProperty {

  @ConfigProperty(name = "test.string.property")
  def testStringProperty: DynamicConfigPropertyValue[String]
}

trait ConfigProviderWithVariousProperties {

  @ConfigProperty(name = "test.string.property")
  def testStringProperty: ConfigPropertyValue[String]

  @ConfigProperty(name = "test.integer.property")
  def testIntegerProperty: ConfigPropertyValue[Integer]

  @ConfigProperty(name = "test.long.property")
  def testLongProperty: ConfigPropertyValue[java.lang.Long]

  @ConfigProperty(name = "test.boolean.property")
  def testBooleanProperty: ConfigPropertyValue[java.lang.Boolean]

  @ConfigProperty(name = "test.list.string.property")
  def testStringListProperty: ConfigPropertyValue[java.util.List[String]]

  @ConfigProperty(name = "test.list.integer.property")
  def testIntegerListProperty: ConfigPropertyValue[java.util.List[Integer]]

  @ConfigProperty(name = "test.list.long.property")
  def testLongListProperty: ConfigPropertyValue[java.util.List[java.lang.Long]]

  @ConfigProperty(name = "test.list.boolean.property")
  def testBooleanListProperty: ConfigPropertyValue[java.util.List[java.lang.Boolean]]
}

class PropertySourceMocker(mockPropertySource: PropertySource) {

  val propertyNames: ListBuffer[String] = new ListBuffer[String]()

  def containing(property: (String,String)): PropertySourceMocker = {
    propertyNames.append(property._1)
    Mockito.when(mockPropertySource.getPropertyNames).thenReturn(propertyNames)
    Mockito.when(mockPropertySource.getValue(property._1)).thenReturn(Optional.of(property._2))
    this
  }

  def withDescription(description: String): PropertySourceMocker = {
    Mockito.when(mockPropertySource.getDescription).thenReturn(description)
    this
  }

  def containingNoProperties = {
    propertyNames.clear()
    Mockito.when(mockPropertySource.getPropertyNames).thenReturn(propertyNames)
    this
  }
}


