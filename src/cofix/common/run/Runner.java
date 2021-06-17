/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.run;

import java.util.List;
import java.util.Map;

import cofix.common.config.Constant;
import cofix.common.util.LevelLogger;
import cofix.common.util.Pair;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.main.Timer;

/**
 * @author Jiajun
 * @date Jul 11, 2017
 */
public class Runner {
	
	private final static String __name__ = "@Runner ";
	private final static String SUCCESSTEST = "Failing tests: 0";
	
	
	public static boolean testSingleTest(Subject subject, String clazzAndMethod, boolean record, int timeout){
		List<String> message = null;
		System.out.println("TESTING : " + clazzAndMethod);
		long t0 = System.currentTimeMillis();
		try {
			message = Executor.execute(CmdFactory.createTestSingleTestCaseCmd(subject, timeout, clazzAndMethod));
		} catch (Exception e) {
			LevelLogger.fatal(__name__ + "#buildSubject run test single test case failed !", e);
		}
		
		long testTime = System.currentTimeMillis() - t0;
		
		boolean success = false;
		for(int i = message.size() - 1; i >= 0; i--){
//			System.out.println(message.get(i));
			if (message.get(i).contains(SUCCESSTEST)) {
				success = true;
				break;
			}
		}
		if (record)
		{
			subject.getRecorder().getTestsExecutionMap().put(clazzAndMethod, new Pair(success ? "SUCCESS" : "FAIL", testTime));
		}
		return success;
	}

	public static boolean runTestSuite(Subject subject, boolean record){
		boolean success = true;
		List<String> message = null;
		try {
			System.out.println("TESTING : " + subject.getName() + "_" + subject.getId());
			//get all tests in the test suite and run them one by one
			long timer = 10*60*1000;	//10 minutes in total for the test suite
			assert subject.getTestCases().size() > 0;
			for (String testCase : subject.getTestCases())
			{
				int timeout = (int)timer/1000 < 30 ? (int)timer/1000 : 30;
				long t0 = System.currentTimeMillis();
				// testSingleTest(subject, testCase, record, (int)timer/1000);
				testSingleTest(subject, testCase, record, timeout);
				long testTime = System.currentTimeMillis() - t0;
				System.out.println(testCase + " run time: " + testTime);
				timer -= testTime;
				assert timer > 0;
			}
		} catch (Exception e) {
			LevelLogger.fatal(__name__ + "#buildSubject run test single test case failed !", e);
		}

		return success;
	}
	
	public static boolean testSingleTest(Subject subject, String clazz, String method, boolean record, int timeout){
		return testSingleTest(subject, clazz + "::" + method, record, timeout);
	}
	
// 	public static boolean runTestSuite(Subject subject){
// 		List<String> message = null;
// 		try {
// 			System.out.println("TESTING : " + subject.getName() + "_" + subject.getId());
// 			message = Executor.execute(CmdFactory.createTestSubjectCmd(subject, 10*60));
// 		} catch (Exception e) {
// 			LevelLogger.fatal(__name__ + "#buildSubject run test single test case failed !", e);
// 		}
		
// 		boolean success = false;
// 		for(int i = message.size() - 1; i >= 0; i--){
// //			System.out.println(message.get(i));
// 			if (message.get(i).contains(SUCCESSTEST)) {
// 				success = true;
// 				break;
// 			}
// 		}
		
// 		return success;
// 	}
	
	public static boolean compileSubject(Subject subject) {
		List<String> message = null;
		try {
			message = Executor.execute(CmdFactory.createBuildSubjectCmd(subject));
		} catch (Exception e) {
			LevelLogger.fatal(__name__ + "#buildSubject run build subject failed !", e);
		}
		
		boolean success = true;
		for(int i = message.size() - 1; i >= 0; i--){
			if (message.get(i).contains(Constant.ANT_BUILD_FAILED)) {
				success = false;
				break;
			}
		}
		
		return success;
	}
}
