package course.model;

public class RunUtility {
	//LongRunningServlet.java測試請求的執行緒
	public static void run(int secs) {
		try {
			Thread.sleep(secs * 1000);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());			
		}
		
	}
}
