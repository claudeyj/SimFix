package cofix.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static cofix.main.Tag.patch;
import static cofix.main.Tag.test;
import static cofix.main.Tag.time;
import static cofix.main.Tag.status;
import static cofix.main.Tag.name;
import static cofix.main.Tag.patchQualifiedName;
import static cofix.main.Tag.patchResult;

enum Tag 
{
    patch,
    test,
    time,
    status,
    name,
    patchQualifiedName,
    patchResult
}

public class XMLReporter {
    private Writer out = null;

    public void report(String patchName, String result, String testsXmlPath)
    {
        File testsXml = new File(testsXmlPath);
        try {
            if (!testsXml.exists()) 
            {
                System.err.println("tests temp xml " + testsXmlPath + " not exist!");
                System.exit(0);
            }
            List<String> lines = Files.readAllLines(Paths.get(testsXmlPath), StandardCharsets.UTF_8);
            List<String> testNodes = new ArrayList<>();
            for (String line : lines)
            {
                // System.out.println(line);
                String strippedLine = line.replace("\t", "").replace("\n", "");
                if (strippedLine.trim().startsWith("<test>")) testNodes.add(strippedLine.trim());
            }
            if (testNodes.size() == 0) throw new Exception();
            if (testNodes.size() == 1)
            {
                out.write(makeNode(makeNode(patchName, patchQualifiedName) + makeNode(result, patchResult) + testNodes.get(0), patch) + "\n");
            }
            if (testNodes.size() > 1)
            {
                String tests = "\n";
                for (String testNode : testNodes)
                {
                    tests += ("\t" + testNode + "\n");
                }
                out.write(makeNode(makeNode(patchName, patchQualifiedName) + makeNode(result, patchResult) + tests, patch) + "\n");
            }
            testsXml.delete();
        } catch (Exception e) {
            e.printStackTrace();
            testsXml.delete();
            System.exit(0);
        }
    }

    public void runStart(String xmlReportPath)
    {
        try {
            File xmlReport = new File(xmlReportPath);
            File parentDir = xmlReport.getParentFile();
            parentDir.mkdirs();
            xmlReport.delete();
            this.out = new FileWriter(xmlReportPath);
            write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            write("<patches>\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void write(final String value) {
        try {
            out.write(value);
        } catch (final IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private String makeNode(final String value, final Tag tag) {
        if (value != null) {
            return "<" + tag + ">" + value + "</" + tag + ">";
        } else {
            return "<" + tag + "/>";
        }
    }

    public void runEnd()
    {
        try {
            write("</patches>\n");
            this.out.close();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }
}
