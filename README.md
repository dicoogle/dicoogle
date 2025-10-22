# <img src="https://user-images.githubusercontent.com/4738426/33545371-e652d482-d8d5-11e7-9ea5-c676d9313378.png" height="50"/>

[![Java CI with Maven](https://github.com/bioinformatics-ua/dicoogle/actions/workflows/maven.yml/badge.svg)](https://github.com/bioinformatics-ua/dicoogle/actions/workflows/maven.yml)
[![Build Webapp with Node.js](https://github.com/bioinformatics-ua/dicoogle/actions/workflows/node.yml/badge.svg)](https://github.com/bioinformatics-ua/dicoogle/actions/workflows/node.yml)

> This is the development branch for Dicoogle 3. [See here](https://github.com/bioinformatics-ua/dicoogle/tree/release/2.X) for the development line of Dicoogle 2, which is being phased out.

Dicoogle is an extensible, platform-independent and open-source PACS archive software that replaces the traditional centralized database with a more agile indexing and retrieval mechanism. It was designed to support automatic extraction, indexing and storage of all meta-data detected in medical images, including private DICOM attribute tags, without re-engineering or reconfiguration requirements.

The architecture of Dicoogle is described in the following articles:

- Lebre, R., Pinho, E., Jesus, R. , Silva, L.A.B., Costa, C. _Dicoogle Open Source: The Establishment of a New Paradigm in Medical Imaging_. 2022 Journal of Medical Systems 46: 77. doi:10.1007/s10916-022-01867-3 [[link](https://link.springer.com/article/10.1007/s10916-022-01867-3)]
- Lebre, R., Pinho, E., Silva, J.M., Costa, C. _Dicoogle Framework for Medical Imaging Teaching and Research_. 2020 IEEE Symposium on Computers and Communications (ISCC) [[link](https://www.researchgate.net/publication/337703675_Dicoogle_Framework_for_Medical_Imaging_Teaching_and_Research)]
- Valente, F., Silva, L.A.B., Godinho, T.M., Costa, C. _Anatomy of an Extensible Open Source PACS_. J Digit Imaging (2016) 29: 284. doi:10.1007/s10278-015-9834-0 [[link](https://link.springer.com/article/10.1007/s10278-015-9834-0)]

Our official website is at https://www.dicoogle.com. A few essential plugins for Dicoogle are available to download there, as well as a built jar of the Dicoogle platform. To learn how to use Dicoogle, please see our [Learning Pack](https://bioinformatics-ua.github.io/dicoogle-learning-pack). To build the core platform yourself, please see the section on [Building Dicoogle](#building-dicoogle) below. The learning pack also has a page on [Building Dicoogle](https://bioinformatics-ua.github.io/dicoogle-learning-pack/docs/building/).

Brief Documentation
-------------------

#### Setup Dicoogle Platform Environment
  1. Copy dicoogle.jar to the installation folder. For example DicoogleDir/
  2. Create the Folder DicoogleDir/Plugins.

      This folder will hold the plugins used by our instance of the Dicoogle Platform.
  3. Next, copy the desired plugins into the DicoogleDir/Plugins Folder.
      
      The typical setup of Dicoogle involves the deployment of an Indexing and Query Plugin. 
      We supply an implementation of such plugin based in Apache Lucene. 
  4. Run Dicoogle.
  
      Dicoogle may be run as a server: ```java -jar dicoogle.jar -s```

      To load the server and open Dicoogle's user interface with the default browser: ```java -jar dicoogle.jar```

#### Available Plugins
  
  We provide a few plugins at the official website, in the [Downloads](https://dicoogle.com/downloads/) page.

  * Lucene Index/Query Plugin - (lucene.jar)
      
      Plugin Based on Apache Lucene to support indexing and querying of DICOM meta-data. With this plugin set, it is possible to index nearly meta-data and perform free text, keyword-based, and range-based queries.
      
  * File Storage Plugin - (filestorage.jar)

      Plugin used for the storage of DICOM Files. This plugin is necessary in order to use Dicoogle as a complete DICOM Storage Provider. The core platform provides a fallback implementation which supports reading (but not storing) files from the local file system.
      
      For storage purposes, our file storage plugin maps the DICOM hierarchical organization (Patient->Study->Series->Image) into a directory tree in the file system. Every object in the Dicoogle Platform may be traced back to its storage location by a URI, similar to file:/tmp/file. In order to support multiple providers, every Storage plugin must define a unique scheme, which maps to the protocol used to store and retrieve content. 
      
      * Settings
      
        * `root-dir`: is the root directory where DICOM Files will be stored
        * `scheme`: Specifies the scheme/protocol of the file plugin. This value is arbitrary, but must be unique among all installed plugins. As such, avoid using well known protocol names such as http or file.

#### Configuring Plugins

  Plugin configurations are accessible via "/DicoogleDir/Plugins/settings/_PluginName_.xml", where _PluginName_ stands for the name of the plugin.
  Upon initialization, if no configurations file is supplied, a new one with the default values is created.

#### Using the Web Application

  * Configuring Services

      In the Management Page, Services and Plugins settings, it is possible to start and/or stop currently running services in real time. Moreover, some configurations like the DICOM service ports may be set.

  * Index a Directory

      Indexing a directory is done simply by accessing the Indexer page, on the side bar. 
      In this page, one can select a root directory to index. The path is a URI defined according to the storage provider, and defaults to the `file` scheme.
      
      In the Management pange, one may also enable the Dicoogle Directory Watcher, which creates a daemon that listens for new files in the root directory.
      After selecting the configurations, the "Apply Settings" button must be pressed.
      When the right settings are saved, the Start buttons fires the indexing process. Please note that this process may take considerable time to complete.

  * Using the Search Interface

      The search page enables users to execute queries over the indexed meta-data.
      The query syntax is similar to the Lucene's Tag:Value query format, but free text searches are also supported. For inexperienced users, an advanced input module may also be used.
      
      In the search interface, it is also possible to select which providers to query. Query providers are actually Query Plugins, that are installed either in the local instance of Dicoogle, or in remote instances if the platform is using the WAN plugin. Therefore, be careful and select exactly which providers you want to query, in order to retrieve more accurate and faster results.

  * Export Results 

      After running a query, the result browser shows up, giving the user an intuitive hierarchical view of the results. On this page, there is also an Export button, which is used in order to export the query results into a CSV file. When the export button is clicked, the user has to select which tags (s)he wants to export in the CSV file. This selection is heavily assisted by the interface, on which the user may type an incomplete tag and have presented the available candidates that match the inserted term. Moreover, the text box allows users to copy a list of tags directly from another CSV file, enabling an easier generation of reports.

#### Using the Web Services

  Let us assume that the Web Services for our instance of Dicoogle are running in https://demo.dicoogle.com/
 
  * Searching
     Dicoogle provides a flexible web service for querying, under the `/search` endpoint.
  
    * Search by Date Range, Access images in date 2005/03/29

       Query: `"StudyDate:[20050329 TO 20050329]"`
       
       URL: `https://demo.dicoogle.com/search?query=StudyDate:[20050329%20TO%2020050329]`
    
    * Access images in date 2005/03/29 and CT (Computer Tomography) modality
        
       Query: `"Modality:CT AND StudyDate:[20050329 TO 20050329]"`
       
       URL: `https://demo.dicoogle.com/search?query=Modality:CT%20AND%20StudyDate:[20050329%20TO%2020050329]`

    * Free text search, looking for CT keyword

       Query: `CT`
       
       URL: `https://demo.dicoogle.com/search?query=CT`

  * Access the list of attributes of an image (by SOPInstanceUID)

     URL: `https://demo.dicoogle.com/dump?uid=1.3.12.2.1107.5.1.4.54023.30000005032914013107800000965`
  
  * Get a DICOM File

     URL: `https://demo.dicoogle.com/legacy/file?uid=1.3.12.2.1107.5.1.4.54023.30000005032914013107800000965`

  * Return documents from particular query providers (useful for queries that do not follow the typical Lucene query format)
  
    URL: `https://demo.dicoogle.com/search?query=Modality:NM&provider=lucene&provider=mongo`
    
    Parameters:
      - query : Query String
      - provider: name of the query providers - multiple - optional
         - all: default - asks all available providers.
         - _provider name_: name of the provider, e.g. `lucene`.

  * Force Dicoogle to index a given Resource. (useful when conventional notification systems (DICOM Services, DirectoryMonitoring, Human Interface) fail to start the index procedure)

    URL: `https://demo.dicoogle.com/management/tasks/index?uri=file:/tmp/dataset-ieeta/`
    
    - Method: `POST`
    - Parameters:
      - uri: The root identifier of the resources that will be indexed. Please note that Dicoogle will fetch these resources from a storage plugin. Therefore, a plugin capable of handling these resources must be enabled. The provider is identified by the URI's scheme.

A live demo was deployed at the given URL. Feel free to experiment with these services.

We also have programmatic APIs for interfacing with Dicoogle in [JavaScript](https://github.com/bioinformatics-ua/dicoogle-client-js), [Java](https://github.com/bioinformatics-ua/dicoogle-java), and [Python](https://github.com/bioinformatics-ua/dicoogle-python).

#### Create your own Plugins

  In order to integrate new functionalities in Dicoogle, you may create your own plugin set. A plugin set comprises plugins that are developed with the intent of supporting a given feature, and are packaged in a single jar file for deployment. See the learning pack on [Plugin Development](https://bioinformatics-ua.github.io/dicoogle-learning-pack/docs/developing-plugins/) for our guide (we also have a [wiki page](https://github.com/bioinformatics-ua/dicoogle/wiki/Plugin-Development)), and our [sample plugin project](https://github.com/bioinformatics-ua/dicoogle-plugin-sample) for a base project from which you can start making your own plugins.

### Building Dicoogle 

Before building, please make sure that your system contains the following tools:

 - Java JDK, either Oracle or OpenJDK (at least version 8);
 - Maven 3;

 1. Retrieve the full source code from this repository: `git clone https://github.com/bioinformatics-ua/dicoogle.git`
 2. Navigate to the project's base directory, and build the parent Maven project by calling `mvn install`.
    - Note: this will build the web application using an embedded version of Node.js and npm. To skip building the webapp: `mvn install -Dskip.installnodenpm -Dskip.npm`
 3. The resulting jar file can be found in "./dicoogle/target".


Contributing
------------

The open source project is maintained by [UA.PT Bioinformatics](https://bioinformatics.ua.pt/) and [BMD Software](https://www.bmd-software.com/). Your contributions to the software are also welcome. Dicoogle is sought to be useful for R&D and the industry alike. You may find our [Development Guidelines](https://github.com/bioinformatics-ua/dicoogle/wiki#development-guidelines) in the wiki. Issues containing the [`easy`](https://github.com/bioinformatics-ua/dicoogle/issues?q=is%3Aissue+is%3Aopen+label%3Aeasy) label should be the most suitable for first open source contributions. For tech support, please prefer contacting the maintainers instead of creating an issue.


## Support and consulting
[<img src="https://raw.githubusercontent.com/wiki/BMDSoftware/dicoogle/images/bmd.png" height="64" alt="BMD Software">](https://www.bmd-software.com)

Please contact [BMD Software](https://www.bmd-software.com) for professional support and consulting services.


Project committers
------------------

Maintainers:

* Luís Bastião (BMD Software - development leader) - [@bastiao](https://github.com/bastiao)
* Eduardo Pinho (BMD Software - co-leader) - [@Enet4](https://github.com/Enet4)

Contributors:

* Rui Jesus (BMD Software) - [@Rui-Jesus](https://github.com/Rui-Jesus)
* Tibério Baptista (BMD Software) - [@Tiberio-Baptista](https://github.com/Tiberio-Baptista)

Past developers:

* Carlos Ferreira
* David Campos
* Eriksson Monteiro
* Frederico Silva
* Frederico Valente
* Jorge Miguel Silva
* Leonardo Oliveira
* Luis Ribeiro
* Renato Pinho
* Samuel Campos
* Tiago Godinho
* Rui Lebre

Project leaders
---------------

* Carlos Costa and José Luis Oliveira (UA.PT Bioinformatics, scientific advisors)
* Luís Bastião (BMD software - development)

