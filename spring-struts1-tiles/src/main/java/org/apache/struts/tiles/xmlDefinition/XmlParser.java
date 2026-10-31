package org.apache.struts.tiles.xmlDefinition;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 * Parse an XML definitions file.
 */
public class XmlParser {

    /** Associated digester. */
    protected Digester digester;
    /**
     * Should we use a validating XML parser to read the configuration file.
     * Default is <code>false</code>.
     */
    protected boolean validating = false;
    /**
     * The set of public identifiers, and corresponding resource names for
     * the versions of the configuration file DTDs we know about.  There
     * <strong>MUST</strong> be an even number of Strings in this list!
     */
    protected String []registrations  = {
        "-//Apache Software Foundation//DTD Tiles Configuration 1.1//EN",
        "/org/apache/struts/resources/tiles-config_1_1.dtd",
        "-//Apache Software Foundation//DTD Tiles Configuration 1.3//EN",
        "/org/apache/struts/resources/tiles-config_1_3.dtd",
        "-//Apache Software Foundation//DTD Tiles Configuration 1.4//EN",
        "/org/apache/struts/resources/tiles-config_1_4.dtd"
    };

     /**
      * Constructor.
      * Creates a digester parser and initializes syntax rules.
      */
    public XmlParser() {
        digester = new Digester();
        digester.setValidating(validating);
        digester.setNamespaceAware(true);
        digester.setUseContextClassLoader(true);
    // Register our local copy of the DTDs that we can find
        for (int i = 0; i < registrations.length; i += 2) {
            URL url = this.getClass().getResource(registrations[i + 1]);
            if (url != null) {
                digester.register(registrations[i], url.toString());
            }
        }
    // Init syntax rules
        initDigester(digester);
    }

    /**
     * Set digester validating flag.
     */
    public void setValidating(boolean validating) {
        digester.setValidating(validating);
    }


   /**
    * Init digester for components syntax.
    * This is an old set of rules, left for backward compatibility.
    * @param digester Digester instance to use.
    */
    private void initDigesterForComponentsDefinitionsSyntax(Digester digester) {
     // Common constants
        String packageName = "org.apache.struts.tiles.xmlDefinition";
        String definitionTag = "component-definitions/definition";
        String definitionHandlerClass = packageName + ".XmlDefinition";

        String putTag  = definitionTag + "/put";
        String putAttributeHandlerClass = packageName + ".XmlAttribute";

        String listTag = definitionTag + "/putList";
        String listHandlerClass     = packageName + ".XmlListAttribute";

        String addListEleTag = listTag + "/add";

    // syntax rules
        digester.addObjectCreate(definitionTag, definitionHandlerClass);
        digester.addSetProperties(definitionTag);
        digester.addSetNext(definitionTag, "putDefinition", definitionHandlerClass);
    // put / putAttribute rules
        digester.addObjectCreate(putTag, putAttributeHandlerClass);
        digester.addSetNext(putTag, "addAttribute", putAttributeHandlerClass);
        digester.addSetProperties(putTag);
        digester.addCallMethod(putTag, "setBody", 0);
    // list rules
        digester.addObjectCreate(listTag, listHandlerClass);
        digester.addSetProperties(listTag);
        digester.addSetNext(listTag, "addAttribute", putAttributeHandlerClass);
    // list elements rules
    // We use Attribute class to avoid rewriting a new class.
    // Name part can't be used in listElement attribute.
        digester.addObjectCreate(addListEleTag, putAttributeHandlerClass);
        digester.addSetNext(addListEleTag, "add", putAttributeHandlerClass);
        digester.addSetProperties(addListEleTag);
        digester.addCallMethod(addListEleTag, "setBody", 0);
    }

   /**
    * Init digester for Tiles syntax.
    * Same as components, but with first element = tiles-definitions
    * @param digester Digester instance to use.
    */
    private void initDigesterForTilesDefinitionsSyntax(Digester digester) {
     // Common constants
        String packageName = "org.apache.struts.tiles.xmlDefinition";
        String definitionTag = "tiles-definitions/definition";
        String definitionHandlerClass = packageName + ".XmlDefinition";

        String putTag  = definitionTag + "/put";
        String putAttributeHandlerClass = packageName + ".XmlAttribute";

  //String listTag = definitionTag + "/putList";
    // List tag value
        String listTag = "putList";
        String defListTag = definitionTag + "/" + listTag;
        String listHandlerClass     = packageName + ".XmlListAttribute";
    // Tag value for adding an element in a list
        String addListEleTag = "*/" + listTag + "/add";

    // syntax rules
        digester.addObjectCreate(definitionTag, definitionHandlerClass);
        digester.addSetProperties(definitionTag);
        digester.addSetNext(definitionTag, "putDefinition", definitionHandlerClass);
    // put / putAttribute rules
    // Rules for a same pattern are called in order, but rule.end() are called
    // in reverse order.
    // SetNext and CallMethod use rule.end() method. So, placing SetNext in
    // first position ensure it will be called last (sic).
        digester.addObjectCreate(putTag, putAttributeHandlerClass);
        digester.addSetNext(putTag, "addAttribute", putAttributeHandlerClass);
        digester.addSetProperties(putTag);
        digester.addCallMethod(putTag, "setBody", 0);
    // Definition level list rules
    // This is rules for lists nested in a definition
        digester.addObjectCreate(defListTag, listHandlerClass);
        digester.addSetProperties(defListTag);
        digester.addSetNext(defListTag, "addAttribute", putAttributeHandlerClass);
    // list elements rules
    // We use Attribute class to avoid rewriting a new class.
    // Name part can't be used in listElement attribute.
        digester.addObjectCreate(addListEleTag, putAttributeHandlerClass);
        digester.addSetNext(addListEleTag, "add", putAttributeHandlerClass);
        digester.addSetProperties(addListEleTag);
        digester.addCallMethod(addListEleTag, "setBody", 0);

    // nested list elements rules
    // Create a list handler, and add it to parent list
        String nestedList = "*/" + listTag + "/" + listTag;
        digester.addObjectCreate(nestedList, listHandlerClass);
        digester.addSetProperties(nestedList);
        digester.addSetNext(nestedList, "add", putAttributeHandlerClass);

    // item elements rules
    // We use Attribute class to avoid rewriting a new class.
    // Name part can't be used in listElement attribute.
  //String addWildcard = listTag + "/addItem";
  // non String addWildcard = listTag + "/addx*";
        String addWildcard = "*/item";
        String menuItemDefaultClass = "org.apache.struts.tiles.beans.SimpleMenuItem";
        digester.addObjectCreate(addWildcard, menuItemDefaultClass, "classtype");
        digester.addSetNext(addWildcard, "add", "java.lang.Object");
        digester.addSetProperties(addWildcard);

    // bean elements rules
        String beanTag = "*/bean";
        String beanDefaultClass = "org.apache.struts.tiles.beans.SimpleMenuItem";
        digester.addObjectCreate(beanTag, beanDefaultClass, "classtype");
        digester.addSetNext(beanTag, "add", "java.lang.Object");
        digester.addSetProperties(beanTag);

    // Set properties to surrounding element
        digester.addSetProperty(beanTag + "/set-property", "property", "value");
    }

   /**
    * Init digester in order to parse instances definition file syntax.
    * Instances is an old name for "definition". This method is left for
    * backwards compatibility.
    * @param digester Digester instance to use.
    */
    private void initDigesterForInstancesSyntax(Digester digester) {
        // Build a digester to process our configuration resource
        String packageName = "org.apache.struts.tiles.xmlDefinition";
        String instanceTag = "component-instances/instance";
        String instanceHandlerClass = packageName + ".XmlDefinition";

        String putTag = instanceTag + "/put";
        String putAttributeTag = instanceTag + "/putAttribute";
        String putAttributeHandlerClass = packageName + ".XmlAttribute";

        String listTag     = instanceTag + "/putList";
        String listHandlerClass     = packageName + ".XmlListAttribute";

        String addListEleTag = listTag + "/add";

    // component instance rules
        digester.addObjectCreate(instanceTag, instanceHandlerClass);
        digester.addSetProperties(instanceTag);
        digester.addSetNext(instanceTag, "putDefinition", instanceHandlerClass);
    // put / putAttribute rules
        digester.addObjectCreate(putAttributeTag, putAttributeHandlerClass);
        digester.addSetProperties(putAttributeTag);
        digester.addSetNext(putAttributeTag, "addAttribute", putAttributeHandlerClass);
    // put / putAttribute rules
        digester.addObjectCreate(putTag, putAttributeHandlerClass);
        digester.addSetProperties(putTag);
        digester.addSetNext(putTag, "addAttribute", putAttributeHandlerClass);
    // list rules
        digester.addObjectCreate(listTag, listHandlerClass);
        digester.addSetProperties(listTag);
        digester.addSetNext(listTag, "addAttribute", putAttributeHandlerClass);
    // list elements rules
    // We use Attribute class to avoid rewriting a new class.
    // Name part can't be used in listElement attribute.
        digester.addObjectCreate(addListEleTag, putAttributeHandlerClass);
        digester.addSetProperties(addListEleTag);
        digester.addSetNext(addListEleTag, "add", putAttributeHandlerClass);
    }

   /**
    * Init digester.
    * @param digester Digester instance to use.
    */
    protected void initDigester(Digester digester) {
        initDigesterForTilesDefinitionsSyntax(digester);
        initDigesterForComponentsDefinitionsSyntax(digester);
        initDigesterForInstancesSyntax(digester);
    }

  /**
   * Parse input reader and add encountered definitions to definitions set.
   * @param in Input stream.
   * @param definitions Xml Definitions set to which encountered definition are added.
   * @throws IOException On errors during file parsing.
   * @throws SAXException On errors parsing XML.
   */
    public void parse(InputStream in, XmlDefinitionsSet definitions) throws IOException, SAXException {
        try {
      // set first object in stack
    //digester.clear();
            digester.push(definitions);
      // parse
            digester.parse(in);
            in.close();
        } catch (SAXException e) {
      //throw new ServletException( "Error while parsing " + mappingConfig, e);
            throw e;
        }

    }

    /**
     * Main method to check file syntax.
     */
    public static void main(String[] args) {
  //String filename = "E:/programs/jakarta-tomcat/webapps/wtiles-struts/WEB-INF/tiles-examples-defs.xml";
        String filename = "E:/programs/jakarta-tomcat-4.0.3/webapps/wtiles-struts/WEB-INF/tiles-examples-defs.xml";
  //String filename = "E:/programs/jakarta-tomcat/webapps/wtiles-struts/WEB-INF/tilesDefinitions.xml";
  //String filename = "E:/programs/jakarta-tomcat/webapps/wtiles-channel/WEB-INF/componentDefinitions.xml";
  //String filename2 = "E:/programs/jakarta-tomcat/webapps/wtiles-tutorial/WEB-INF/componentDefinitions.xml";


        if (args.length > 1) {
            filename = args[1];
        } // end if

        System.out.println("Read file '" + filename  + "'");

        InputStream input = null;
  // InputStream input2 = null;
    // Open file
        try {
            input = new BufferedInputStream(
                             new FileInputStream(filename));
    //    input2 = new BufferedInputStream(
          //                   new FileInputStream( filename2) );
        } catch (IOException ex) {
            System.out.println("can't open file '" + filename + "' : " + ex.getMessage());
        }
    // Check file syntax
        if (input == null) {
            System.out.println("no file parsed");
            return;
        }
        try {
            XmlParser parser = new XmlParser();
            parser.setValidating(true);
            XmlDefinitionsSet definitions = new XmlDefinitionsSet();
            System.out.println("  Parse file");
            parser.parse(input, definitions);
      //  System.out.println( "  Check file 2" );
      //parser.parse( input2, definitions);
            System.out.println("  done.");
            System.out.println("  Result : " + definitions.toString());
        } catch (Exception ex) {
            System.out.println("Error during parsing '" + filename + "' : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
