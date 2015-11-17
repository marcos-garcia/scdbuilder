# SCD Builder

This is a [Pentaho Data Integration](http://community.pentaho.com/projects/data-integration/) step extension which builds a [Type 2 Slowly Changing Dimension](https://en.wikipedia.org/wiki/Slowly_changing_dimension#Type_2) from a stream of source data in a snapshot form. The step calculates when an  item has changed and emits rows for every state of the item, with it's initial and final valid date.

In order to be able to do further operations with the previous or next status, it also emits both with a prefix indicated by the user.

The step also gives you the chance to preload an initial [SCD](https://en.wikipedia.org/wiki/Slowly_changing_dimension) before the snapshot stream of data is read.

Another option allowed is to indicate that the stream of data incoming is already ordered, so that the step performance concerning memory usage is optimal and admits a larger amount of data before it overflows.

## Installation

To add this plugin into your PDI installation, you have to download the source or checkout this repository first with this sentence (install [Git](https://git-scm.com/) before)

`git clone https://github.com/marcos-garcia/scdbuilder.git`

You must install [Apache Maven](https://maven.apache.org/) before continuing. Once you have Maven installed, open a command console and go to the *scdbuilder* folder, where *pom.xml* file resides. Run the next command to build binaries and package the jar.

`maven package`

If you have already built a previous version before, you should execute `maven clean` first.

Once the jar is built in the *target* folder it's time to add the plugin to your PDI installation. To do so, you have to tell maven which is your PDI installation folder, writting it's path into a variable called pdi.install.dir when you call the install step. For instance, if your PDI path is *C://Pentaho/data-integration* then, the command you should execute is

`mvn -Dpdi.install.dir=C://Pentaho/data-integration install`

Otherwise, if you're running maven within Eclipse, go to your settings.xml file in the .m2 folder and add this code to insert the pdi.install.dir variable:

```<profiles>
  ...
    <profile>
	  <id>inject-pdi-home</id>
      <properties>
		<pdi.install.dir>C://Pentaho/data-integration</pdi.install.dir>
      </properties>
    </profile>
    ...
  </profiles>
  <activeProfiles>
  ...
    <activeProfile>inject-pdi-home</activeProfile>
  ...
  </activeProfiles>```

Once the plugin is installed, you will be able to use this step into your ETL processes the next time you open Spoon

## Usage

TODO: Write usage instructions

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## Credits

Author: [Marcos García Casado](http://www.marcosgarciacasado.com)

Organization: [Bayes Forecast](http://www.bayesforecast.com)

## License

This work is [free software](https://en.wikipedia.org/wiki/Free_software); you can redistribute it and/or modify it under the terms of the [GNU General Public License](https://en.wikipedia.org/wiki/GNU_General_Public_License) as published by the [Free Software Foundation](https://en.wikipedia.org/wiki/Free_Software_Foundation); either version 2 of the License, or any later version.

This work is distributed in the hope that it will be useful, but **without any warranty**; without even the implied warranty of **merchantability** or **fitness for a particular purpose**. See [version 2](http://www.gnu.org/licenses/old-licenses/gpl-2.0.html) and [version 3 of the GNU General Public License](http://www.gnu.org/copyleft/gpl-3.0.html) for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
