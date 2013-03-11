package org.uiautomation.ios.server.servlet;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriverException;
import org.uiautomation.iosdriver.services.DeviceInstallerService;
import org.uiautomation.iosdriver.services.LoggerService;
import org.uiautomation.iosdriver.services.jnitools.JNILoggerListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArchiveServlet extends DriverBasedServlet {

  private final
  Map<String, ArchiveStatus>
      statuses =
      new ConcurrentHashMap<String, ArchiveStatus>();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(200);
    try {
      String logId = req.getParameter("logId");
      if (logId != null) {
        response.getWriter().write(status(logId));
      } else {
        response.getWriter().write(page());
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    response.getWriter().close();
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse response) throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(200);
    LoggerService.registerListener(new ArchiveActivityListener());

    final DeviceInstallerService
        service =
        new DeviceInstallerService("d1ce6333af579e27d166349dc8a1989503ba5b4f");

    statuses.clear();
    final String logId = "uniqueLogTODO";
    new Thread(new Runnable() {
      @Override
      public void run() {
        /*logId*/
        service.archive("com.yourcompany.UICatalog", false, false,
                        new File("/Users/freynaud/build/archived"), true);
      }
    }).start();

    //service.archive("com.yourcompany.UICatalog", false, false, new File("/Users/freynaud/build/archived"),true);

    response.getWriter().write("uniqueLogTODO");
    response.getWriter().close();
  }

  private String status(String logId) {
    return getStatus(logId).getStatus().toString();
  }

  private String page() throws JSONException {
    return "content";
  }


  public static void main(String[] args) {
    System.out.println(new Message("[uniqueLogTODO]Archive - TakingInstallLock (0%)"));
  }

  class ArchiveActivityListener implements JNILoggerListener {

    @Override
    public void onLog(int level, String message) {
      if (message.contains("Archive")) {
        Message m = new Message(message);
        ArchiveStatus status = getStatus(m.getLogId());
        status.update(m);

      }
    }


  }

  private ArchiveStatus getStatus(String logId) {
    ArchiveStatus res = statuses.get(logId);
    if (res == null) {
      res = new ArchiveStatus(logId);
      statuses.put(logId, res);
    }
    return res;
  }

  class ArchiveStatus {

    private int id = 0;
    private String currentPhase;
    private String currentStep;
    private int progress;
    private final String logId;


    public ArchiveStatus(String logId) {
      this.logId = logId;
    }

    public void update(Message message) {
      id++;
      currentPhase = message.getPhase();
      currentStep = message.getStep();
      if (message.getProgress() != -1) {
        progress = message.getProgress();
      }
    }

    public JSONObject getStatus() {
      JSONObject res = new JSONObject();
      try {
        res.put("id", id);
        res.put("phase", currentPhase);
        res.put("step", currentStep);
        res.put("progress", progress);
        res.put("logId", logId);
      } catch (JSONException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
      return res;
    }

  }
  // Archive
  //  TakingInstallLock
  //  EmptyingApplication
  //  ArchivingApplication
  //  Complete 93%

  // ArchiveCopy -- 95%

  // RemoveArchive
  //  RemovingArchive 96%
  //  Complete 100%
}


class Message {

  private final String logId;
  private final String phase;
  private final String step;
  private int progress;
  private final String raw;


  public Message(String msg) {
    raw = msg.replaceAll("(\\r|\\n)", "");
    logId = getLogId(raw);
    phase = getPhase(raw);
    step = getStep(raw);
    try {
      progress = getProgress(raw, phase, step);
    } catch (Exception e) {
      progress = -1;
    }
  }

  private int getProgress(String msg, String phase, String step) {
    if ("Error occured:".equals(step)) {
      return 100;
    }

    if ("Archive".equals(phase)) {
      String s = msg.split("\\(")[1];
      s = s.replace("%", "");
      s = s.replace(")", "");
      s = s.replace("(\r)", "");
      return Integer.parseInt(s);
    } else if ("ArchiveCopy".equals(phase)) {
      return 95;
    } else if ("RemoveArchive".equals(phase)) {
      if ("RemovingArchive".equals(step)) {
        return 96;
      } else {
        return 100;
      }
    } else {
      throw new WebDriverException("not recognized : " + phase);
    }
  }


  private String getPhase(String message) {
    String s = message.split("]")[1];
    s = s.split(" - ")[0];
    return s;
  }

  private String getStep(String message) {
    String s = message.split(" - ")[1];
    s = s.split("\\(")[0];
    return s;
  }

  private String getLogId(String message) {
    String s = message.split("]")[0];
    s = s.replace("[", "");
    return s;
  }

  public String getLogId() {
    return logId;
  }

  public String getPhase() {
    return phase;
  }

  public String getStep() {
    return step;
  }

  public int getProgress() {
    return progress;
  }

  @Override
  public String toString() {
    return raw + "-->id:" + logId + ",phase:" + phase + ",step:" + step + ",progress:" + progress;
  }
}