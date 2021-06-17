package cofix.common.util;
import java.util.HashMap;
import java.util.Map;

import cofix.core.parser.node.CodeBlock;

public class Recorder {
    private String patchQualifiedName = null;
    private String candidatePatchString = null;
    private Map<String, Pair<String, Long>> testsExecution = null;
    private String patchResult = null;
    // private Map<String, Long> formerFailingTestsEecutionTime = null;
    // private Map<String, Long> testSuiteTestsExecutionTime = null;
    // One recorder object corresponds to one object
    public Recorder(Subject subject, String filePath, Pair<Integer, Integer> range, int candidatePatchId, CodeBlock candidatePatchCodeBlock)
    {
        this.patchQualifiedName = subject.getName() + "-" + subject.getId() + "-" + filePath + "-L" + range.getFirst() + "-L" + range.getSecond() + "-" + candidatePatchId;
        this.candidatePatchString = candidatePatchCodeBlock.toSrcString().toString().replace("\\s*|\t|\r|\n", "");
        this.testsExecution = new HashMap<>();
        // formerFailingTestsEecutionTime = new HashMap<>();
        // testSuiteTestsExecutionTime = new HashMap<>();
    }

    public String getPatchQualifiedName()
    {
        return this.patchQualifiedName;
    }

    public String getCandidatePatchString()
    {
        return this.candidatePatchString;
    }

    public Map<String, Pair<String, Long>> getTestsExecutionMap()
    {
        return this.testsExecution;
    }

    public void setPatchResult(boolean success)
    {
        this.patchResult = success ? "SURVIVED" : "KILLED";
    }

    public String getPatchResult()
    {
        return this.patchResult;
    }

    // public Map<String, Long> getFormerFailingTestsEecutionTimeMap()
    // {
    //     return this.formerFailingTestsEecutionTime;
    // }

    // public Map<String, Long> getTestSuiteTestsExecutionTime()
    // {
    //     return this.testSuiteTestsExecutionTime;
    // }
}
