Octodubstep
===========

A declarative, type safe configuration property management framework for the JVM. Written in Java but can be used in Scala applications too!

Why the name?
-------------

I couldn't think of a good name (usual for me) so went with pretty much the random name generator built into Github. It has a certain ring to it, but you'd be mislead if you assumed I have any interest in dubstep music.

What is it?
-----------

Octodubstep is, in many ways, inspired by the limitations of the config property mechanisms in Spring (e.g. PropertyPlaceholderConfigurer, @Value annotations etc). Octodubstep introduces features such as type safe, annotation driven config interfaces, dynamic property reloading, DRY property usage, and a host of other goodies (more to come in the future!). On top of this, Octodubstep encourages config properties to be neatly structured into separate logical units.

Documentation
-------------

I'm working hard on pretty thorough documentation - watch this space!


A short example
---------------

For those who can't wait for the docs, this is just a very basic example of Octodubstep in use (in Java).

Firstly, create an annotated configuration interface (Octodubstep will create instance automagically):
```java
public interface MyConfigProperties {
  
  @ConfigProperty(name = "my.string.property", defaultValue="foobar")
  Value<String> myStringProperty();
  
  @ConfigProperty(name = "my.integer.property")
  Value<Integer> myIntegerProperty();  
  
  @ConfigProperty(name = "my.list.property", required="true")
  Value<List<String>> myListProperty();
  
  @ConfigProperty(name = "my.dynamic.string.property")
  DynamicValue<String> myDynamicStringProperty();
}
```
Then, let's see how this can be used in other code:

```java
public class ConfigTester {
  
  private MyConfigProperties properties;
  
  public ConfigTest(PropertyManager manager) {
    properties = manager.propertiesFor(MyConfigProperties.class);
  }
  
  public void doStuff() {
    System.out.println(properties.myStringProperty().currentValue());
  }
}
```

License
-------

This software is licensed under the Apache 2 license, quoted below.

Copyright &copy; 2013 **[Andrew Easter](http://www.dreweaster.com/)**.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

