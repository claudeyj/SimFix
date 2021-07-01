package cofix.common.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import cofix.common.config.Constant;

public class XMLUtils {
    final static String templatePath = Constant.HOME + "/test-time-template.xml";

    public static void clearAndCopy(String testsXmlPath) throws IOException
    {
        File templateFile = new File(templatePath);
        File xmlFile = new File(testsXmlPath);
        if (xmlFile.exists()) xmlFile.delete();
        Files.copy(templateFile.toPath(), xmlFile.toPath());
    }

}