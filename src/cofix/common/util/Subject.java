/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import cofix.common.config.Constant;
import cofix.common.run.CmdFactory;
import cofix.common.run.Executor;
import cofix.common.run.Runner;
import cofix.core.parser.node.CodeBlock;

/**
 * 
 * @author Jiajun
 *
 */
public class Subject {

	private String _name = null;
	private int _id = 0;
	private String _ssrc = null;
	private String _tsrc = null;
	private String _sbin = null;
	private String _tbin = null;
	private List<String> _instrumentPackages = null;
	private List<String> _testClasses = null;
	private List<String> _dependency = null;
	private List<String> _testCases = null;
	private Recorder recorder = null;
	private XMLReporter reporter = null;

	/**
	 * subject
	 * 
	 * @param name
	 *            : name of subject, e.g., "chart".
	 * @param id
	 *            : number of subject, e.g., 1.
	 * @param ssrc
	 *            : relative path for source folder, e.g., "/source"
	 * @param tsrc
	 *            : relative path for test folder, e.g., "/tests"
	 * @param sbin
	 *            : relative path for source byte code, e.g., "/classes"
	 * @param tbin
	 *            : relative path for test byte code, e.g., "/test-classes"
	 */
	public Subject(String name, int id, String ssrc, String tsrc, String sbin, String tbin) {
		this(name, id, ssrc, tsrc, sbin, tbin, null);
	}
	
	public Subject(String name, int id, String ssrc, String tsrc, String sbin, String tbin, List<String> dependency) {
		_name = name;
		_id = id;
		_ssrc = ssrc;
		_tsrc = tsrc;
		_sbin = sbin;
		_tbin = tbin;
		_dependency = dependency;
		initializeSubject(this);
	}
	
	public void setDependency(List<String> dependency){
		_dependency = dependency;
	}

	public String getName() {
		return _name;
	}

	public int getId() {
		return _id;
	}

	public String getSsrc() {
		return _ssrc;
	}

	public String getTsrc() {
		return _tsrc;
	}

	public String getSbin() {
		return _sbin;
	}

	public String getTbin() {
		return _tbin;
	}
	
	public List<String> getDependency(){
		return _dependency;
	}
	
	public List<String> getTestClasses(){
		if(_testClasses == null){
			_testClasses = new ArrayList<>(getTestClasses(new File(getHome() + _tsrc)));
		}
		return _testClasses;
	}

	public List<String> getInstrumentPackage(){
		if(_instrumentPackages == null){
			_instrumentPackages = new ArrayList<>(getPackage(getHome() + _ssrc, getHome() + _ssrc));
		}
		return _instrumentPackages;
	}
	
	/**
	 * get absolute home path for subject
	 * 
	 * @return e.g., "/home/user/chart/chart_1_buggy"
	 */
	public String getHome() {
		return Constant.PROJECT_HOME + _name + "/" + _name + "_" + _id + "_buggy";
	}
	
	public String getFailedTestRecFile(){
		return getHome() + "/failing_tests";
	}
	
	public String getAllTestRecFile(){
		return getHome() + "/all-tests.txt";
	}
	
	
	public String getBuggyLineSuspFile(){
		return Constant.LOCATOR_SUSP_FILE_BASE + "/" + _name + "/" + _id + "/" + "stmt-susps.txt";
	}
	
	public void backup(String folder) throws IOException{
		File file = new File(folder + "_ori");
		if (!file.exists()) {
			FileUtils.copyDirectory(new File(folder), file);
		} else {
			FileUtils.deleteDirectory(new File(folder));
			FileUtils.copyDirectory(file, new File(folder));
		}
	}
	
	public void restore(String folder) throws IOException{
		File file = new File(folder + "_ori");
		if (file.exists()) {
			FileUtils.copyDirectory(file, new File(folder));
		} else {
			System.out.println("Restore source file failed : cannot find file " + file.getAbsolutePath());
		}
	}
	
	public void backup() throws IOException{
		String src = getHome() + _ssrc;
		File file = new File(src + "_ori");
		if (!file.exists()) {
			FileUtils.copyDirectory(new File(src), file);
		} else {
			FileUtils.deleteDirectory(new File(src));
			FileUtils.copyDirectory(file, new File(src));
		}
	}
	
	public void restore() throws IOException{
		String src = getHome() + _ssrc;
		File file = new File(src + "_ori");
		if (file.exists()) {
			FileUtils.copyDirectory(file, new File(src));
		} else {
			System.out.println("Restore source file failed : cannot find file " + file.getAbsolutePath());
		}
	}
	
	private Set<String> getPackage(String rootPath, String currPath){
		Set<String> packages = new HashSet<>();
		File file = new File(currPath);
		File[] files = file.listFiles();
		for(File f : files){
			if (f.getName().equals(".DS_Store")) {
				continue;
			}
			if(f.isDirectory()){
				String absPath = f.getAbsolutePath();
				String packageName = absPath.replace(rootPath + "/", "");
				packageName = packageName.replace("/", ".");
				packages.add(packageName);
				packages.addAll(getPackage(rootPath, f.getAbsolutePath()));
			}
		}
		return packages;
	}
	
	
	private Set<String> getTestClasses(File root){
		Set<String> classes = new HashSet<>();
		File[] files = root.listFiles();
		String pack = null;
		for(File f : files){
			if(f.isFile()){
				String fName = f.getName();
				if (fName.equals(".DS_Store")) {
					continue;
				}
				if(fName.endsWith(".java")){
					fName = fName.substring(0, fName.length() - 5);
					if(!fName.endsWith("Tests")){
						continue;
					}
					if(pack != null){
						classes.add(pack + "." + fName);
					} else {
						BufferedReader br = null;
						try {
							br = new BufferedReader(new FileReader(f));
							String line = null;
							while((line = br.readLine()) != null){
								line = line.trim();
								if(line.startsWith("package")){
									Pattern pattern = Pattern.compile("(?<=package\\s)[\\s\\S]*(?=;)");
									Matcher matcher = pattern.matcher(line);
									if(matcher.find()){
										pack = matcher.group(0);
										classes.add(pack + "." + fName);
										break;
									}
								}
							}
							br.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}else if(f.isDirectory()){
				classes.addAll(getTestClasses(f));
			}
		}
		return classes;
	}

	public List<String> getTestCases()
	{
		return this._testCases;
	}

	private static void initializeSubject(Subject subject)
	{
		System.out.println(subject.getHome());
		System.out.println(subject.getId());
		Executor.execute(new String[]{"/bin/bash", "-c", "rm -rf " + subject.getHome()});
		Executor.execute(CmdFactory.createCheckOutCmd(subject));
		if(!Runner.compileSubject(subject)){
			System.err.println("Build failed !");
		}
		//test to get all test list
		// Executor.execute(CmdFactory.createBuildSubjectCmd(subject));
		// Executor.execute(CmdFactory.createTestSubjectCmd(subject, 0));
		try {
			System.out.println("TESTING : " + subject.getName() + "_" + subject.getId());
			Executor.execute(CmdFactory.createTestSubjectCmd(subject, 0)); //run without timeout limit
		} catch (Exception e) {
			e.printStackTrace();
		}
		// File failingTestFile = new File(subject.getHome() + "/all_tests");
		try {
			String lines = new String(Files.readAllBytes(Paths.get(subject.getHome() + "/all_tests")));
			String[] tests = lines.split("\n");
			System.out.println("number of all tests: " + tests.length);
			assert tests.length > 1;
			if (subject._testCases == null)
			{
				subject._testCases = new ArrayList<>();
			}
			for (String test : tests)
			{
				// System.out.println(test);
				String testMethod = test.split("\\(")[0];
				String temp = test.split("\\(")[1];
				String testClass = temp.substring(0, temp.length() - 1);
				String testCase = testClass + "::" + testMethod;
				// System.out.println(testCase);
				subject._testCases.add(testCase);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		subject.reporter = new XMLReporter();
		subject.reporter.runStart(subject);
	}

	public void setRecorder(Recorder recorder)
	{
		this.recorder = recorder;
	}

	public Recorder getRecorder()
	{
		return this.recorder;
	}

	public XMLReporter getXMLReporter()
	{
		return this.reporter;
	}

	public void clearRecorder()
	{
		this.recorder = null;
	}

	@Override
	public String toString() {
		return "Subject [_name=" + _name + ", _id=" + _id + ", _ssrc=" + _ssrc + ", _tsrc=" + _tsrc + ", _sbin=" + _sbin
				+ ", _tbin=" + _tbin + ", _dependency=" + _dependency + "]";
	}
	
}