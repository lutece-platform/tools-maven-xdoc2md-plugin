!["build status"]("http://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=tools-maven-xdoc2md-plugin-deploy")
# xDoc2md Maven Plugin

## Introduction

This Maven plugin can convert xDoc files (used by Maven Site) to [Markdown](http://daringfireball.net/projects/markdown/) files (Wiki format used by [GitHub](https://guides.github.com/features/mastering-markdown/) ).

![Markdown Logo](https://github.com/dcurtis/markdown-mark/blob/master/png/208x128.png?raw=true)

The goal **readme** can create the `README.md` file from the `src/site/xdoc/index.xml` .

## Usage

Download this project and install the plugin:

```

 mvn install
                    
```

Then go into your project's directory where you want to create or update the `README.md` file.

Type the following command to generate the README.md :

```

 mvn fr.paris.lutece.tools:xdoc2md-maven-plugin:readme
                    
```


[Maven documentation and reports](http://dev.lutece.paris.fr/plugins/xdoc2md-maven-plugin/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*
