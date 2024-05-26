import za.co.mahlaza.research.templateparsing.TemplateReader;
import za.co.mahlaza.research.templateparsing.URIS;
import za.co.mahlaza.research.grammarengine.nguni.zu.ZuluFeatureParser;

import java.util.List;
import java.util.Collections;

import za.co.mahlaza.research.grammarengine.base.interfaces.SlotFiller;
import za.co.mahlaza.research.grammarengine.base.models.template.Slot;
import za.co.mahlaza.research.grammarengine.base.models.template.Template;
import za.co.mahlaza.research.grammarengine.base.models.template.TemplatePortion;
import za.co.mahlaza.research.grammarengine.base.models.feature.Feature;

public class TemplateLoader {

    public static Template getTemplate(String templateName, String templateURI, String templatePath) throws Exception {
        // Initialize the TemplateReader with the ZuluFeatureParser
        TemplateReader.Init(new ZuluFeatureParser());
        // Set the ontology namespace
        TemplateReader.setTemplateOntologyNamespace(URIS.ToCT_NS);
        // Enable debug mode
        TemplateReader.IS_DEBUG_ENABLED = true;
        // Parse the template
        Template template = TemplateReader.parseTemplate(templateName, templateURI, templatePath);
        return template;
    }

    public static void main(String[] args) {
        // Define the arguments based on the local template
        String templatePath = "res/adjectivaltemplate.ttl"; // Path to the TTL file
        String templateName = "adjectTemplate"; // Template name within the TTL file
        String templateURI = "http://people.cs.uct.ac.za/~zmahlaza/templates/test/"; // Base URI defined in the TTL file

        try {
            // Load the template
            Template template = getTemplate(templateName, templateURI, templatePath);
            System.out.println("Template loaded successfully: " + template);
        
            // Generate the final sentence
            StringBuilder sentence = new StringBuilder();
            for (TemplatePortion part : template.getTemplatePortions()) {
                sentence.append(part.toString()).append(" ");
            }

            System.out.println("Generated Sentence: " + sentence.toString().trim().replace("[","").replace("]",""));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
