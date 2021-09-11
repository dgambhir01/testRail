package TestProject.TestProject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;

public class TestPro {

	String PROJECT_ID = "1";
	APIClient client = null;
	public static Properties propLog, propConfig;
	public static final int TEST_CASE_PASSED_STATUS = 1;
	public static final int TEST_CASE_FAILED_STATUS = 5;
	String propFilePath=System.getProperty("user.dir")+"/src/test/java/TestProject/TestProject/login.properties";
	
	TestPro()
	{
		try {
			propConfig = new Properties();
			System.out.println(propFilePath);
			FileInputStream ipConfig = new FileInputStream(propFilePath);
			propConfig.load(ipConfig);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@BeforeSuite
	public void createSuite(ITestContext ctx) throws IOException, AccessException, APIException {
		client = new APIClient(propConfig.getProperty("url"));
		client.setUser(propConfig.getProperty("username"));
		client.setPassword(propConfig.getProperty("password"));
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("include_all", true);
		data.put("name", "Test Run " + System.currentTimeMillis());
		JSONObject c = null;
		c = (JSONObject) client.sendPost("add_run/" + PROJECT_ID, data);
		Long suite_id = (Long) c.get("id");
		ctx.setAttribute("suiteId", suite_id);
	}
	
	
	@BeforeMethod
	public void beforeTest(ITestContext ctx,Method method) throws NoSuchMethodException {
	    Method m = TestPro.class.getMethod(method.getName());
	    if (m.isAnnotationPresent(TestRails.class)) {
	        TestRails ta = m.getAnnotation(TestRails.class);
	        System.out.println("Test case is: " + ta.id());
	        ctx.setAttribute("caseId",ta.id());
	    }
	}

	
	@AfterMethod
	public void afterTest(ITestResult result, ITestContext ctx) throws IOException, APIException {
	    Map<String, Object> data = new HashMap<String, Object>();
	    if(result.isSuccess()) {
	        data.put("status_id",TEST_CASE_PASSED_STATUS);
	    }
	    else{
	        data.put("status_id",TEST_CASE_FAILED_STATUS);
	        data.put("comment", result.getThrowable().toString());
	        }
	    String caseId = (String)ctx.getAttribute("caseId");
	    Long suiteId = (Long)ctx.getAttribute("suiteId");
	    System.out.println("Case Id is: " + caseId + " and suiteID is: " + suiteId);
	    client.sendPost("add_result_for_case/"+suiteId+"/"+caseId,data);
	}
	
	@TestRails(id = "1")
	@Test
	public void validLogin() {
		Assert.assertTrue(true);
	}

	@TestRails(id = "2")
	@Test
	public void invalidLogin() {
		Assert.assertTrue(false);
	}
	
	@TestRails(id = "3")
	@Test
	public void obsoleteLogin() {
		Assert.assertTrue(true);
	}
	
	@TestRails(id = "4")
	@Test
	public void demoLogin() {
		Assert.assertTrue(false);
	}

}
