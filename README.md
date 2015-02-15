
#xDoc2MD Maven Plugin

##Introduction
This Maven plugin can convert xDoc files (used by Maven Site) to Markdown files (Wiki format used by GitHub).The goal **readme** can create the **README.md** file from the **src/site/xdoc/index.xml** .
###Usage
Download the project and install the plugin:
```

 mvn install
                    
```
Then go into your project's directory where you want to create or update the README.md file.Type the following command to generate the README.md :
```

 mvn fr.paris.lutece.tools:xdoc2md-maven-plugin:readme
                    
```
