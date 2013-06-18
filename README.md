AppsGate-server
===============

Git repository of the server part for the AppsGate project.

This Git reposiroty is devided in three parts.

The first one named sources contain all the source code for the serveur part of
the AppsGate project.

The second part named distribution is the current valid distribution for the server part.
It contain an OSGi felix distribution with all working bundles. Some bundle coming from
maven repositories others from industrial partners of the Appsgate projet so that impossible
to provide the source code of those bundles.
By downloading the distribution you can launch the server part of the AppsGate projet on any 
java virtual machine.

The third one is the java documentation for all components.

The master branch correspond to the current release version of the appsGate projet so it
has been tested and it is considered as stable version.

If you encountered problems, please report bug to any appsgate contributor.  


Code guidelines
===============

• Package name appsGate: appsgate.lig.“fonction“.“type“.“ApAMConvention“
  ex: 
  appsgate.lig.temperature.sensor.spec.TemperatureSensorSpec
  appsgate.lig.temperature.sensor.messages.TemperatureNotificationMsg
  or
  appsgate.lig.mediarenderer.service.spec.MediaRendererSpec
  appsgate.lig.mediarenderer.service.messages.MediaRendererNotificationMsg
  
  
• Java/ApAM conventions:

  Specification
  developer: ….Spec
  auto-generate: _spec
  
  Implementation
  developer: …Impl
  
  Instance
  developer: …Inst
  auto-generate: _inst
  
  Interface
  developer: nothing
  
  Message
  developer: …Msg
  
  Composite
  developer: …Composite
  auto-generate: _compo
  
  Composite instance
  developer: …CompositeInst
  
  Classe
  developer: …Impl

• Current device type code:

  0: Temperature
  1: Illumination
  2: Switch
  3: Contact
  4: Key-Card
  5: Motion
  6: Gygogne socket
  7: Color light
  ?: AvTransport
  ?: RenderingControl
  2052964255 : ConnectionManager
  794225618 : ContentDirectory
