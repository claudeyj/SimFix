package cofix.common.util;

import static cofix.common.util.Tag.patch;
import static cofix.common.util.Tag.test;
import static cofix.common.util.Tag.time;
import static cofix.common.util.Tag.status;
import static cofix.common.util.Tag.name;
import static cofix.common.util.Tag.patchQualifiedName;
import static cofix.common.util.Tag.patchResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;


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
    private String currentPatch = null;


    public XMLReporter()
    {

    }

    public void report(Subject subject)
    {
        if (subject.getRecorder() == null || subject.getRecorder().getTestsExecutionMap().size() ==0) return;
        //write tests with execution time and result to the xml file
        String tests = "";
        Map<String, Pair<String, Long>> testsExecution = subject.getRecorder().getTestsExecutionMap();
        for (Map.Entry<String, Pair<String, Long>> entry : testsExecution.entrySet())
        {
            tests += makeNode(makeNode(entry.getKey(), name) + makeNode(entry.getValue().getFirst(), status)
             + makeNode(entry.getValue().getSecond().toString() + "ms", time), test);
        }
        String patchNameNode = makeNode(subject.getRecorder().getPatchQualifiedName(), patchQualifiedName);
        String patchResultNode = makeNode(subject.getRecorder().getPatchResult(), patchResult);
        write(makeNode(patchNameNode + patchResultNode + tests, patch) + "\n");
        subject.clearRecorder();
    }

    private void write(final String value) {
        try {
            out.write(value);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private String makeNode(final String value, final String attributes, final Tag tag) {
        if (value != null) {
            return "<" + tag + " " + attributes + ">" + value + "</" + tag + ">";
        } else {
            return "<" + tag + attributes + "/>";
        }

    }

    private String makeNode(final String value, final Tag tag) {
        if (value != null) {
            return "<" + tag + ">" + value + "</" + tag + ">";
        } else {
            return "<" + tag + "/>";
        }
    }

    public void runStart(Subject subject)
    {
        String buggyProgramHomePath = subject.getHome();
        String xmlPath = buggyProgramHomePath + "/test_time_report/report.xml";
        File xmlReport = new File(xmlPath);
        if (!xmlReport.getParentFile().exists())
        {
            xmlReport.getParentFile().mkdirs();
        }
        if (!xmlReport.exists())
        {
            try {
                xmlReport.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            this.out = new FileWriter(xmlReport);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        write("<patches>\n");
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
