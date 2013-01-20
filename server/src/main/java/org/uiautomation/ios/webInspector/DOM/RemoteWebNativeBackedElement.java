/*
 * Copyright 2012 ios-driver committers.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the Licence at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

package org.uiautomation.ios.webInspector.DOM;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.uiautomation.ios.UIAModels.UIAElement;
import org.uiautomation.ios.UIAModels.UIAWebView;
import org.uiautomation.ios.UIAModels.predicate.AndCriteria;
import org.uiautomation.ios.UIAModels.predicate.Criteria;
import org.uiautomation.ios.UIAModels.predicate.LabelCriteria;
import org.uiautomation.ios.UIAModels.predicate.NameCriteria;
import org.uiautomation.ios.UIAModels.predicate.OrCriteria;
import org.uiautomation.ios.UIAModels.predicate.TypeCriteria;
import org.uiautomation.ios.client.uiamodels.impl.RemoteUIADriver;
import org.uiautomation.ios.communication.device.Device;
import org.uiautomation.ios.mobileSafari.NodeId;
import org.uiautomation.ios.context.WebInspector;
import org.uiautomation.ios.server.ServerSideSession;
import org.uiautomation.ios.server.application.AppleLocale;
import org.uiautomation.ios.server.application.ContentResult;

import java.util.List;
import java.util.logging.Logger;

public class RemoteWebNativeBackedElement extends RemoteWebElement {


  private static final Logger log = Logger.getLogger(RemoteWebNativeBackedElement.class.getName());
  private final ServerSideSession session;
  private final RemoteUIADriver nativeDriver;

  public RemoteWebNativeBackedElement(NodeId id, WebInspector inspector,
                                      ServerSideSession session) {
    super(id, inspector);
    this.session = session;
    this.nativeDriver = session.getNativeDriver();
  }

  private void clickNative() throws Exception {
    ((JavascriptExecutor) nativeDriver).executeScript(getNativeElementClickOnIt());
  }

  private String getNativeElementClickOnIt() throws Exception {
    // web stuff.
    scrollIntoViewIfNeeded();
    Point po = findPosition();

    Dimension dim = inspector.getSize();
    int webPageWidth = inspector.getInnerWidth();
    if (dim.getWidth() != webPageWidth) {
      log.warning("BUG : dim.getWidth()!=webPageWidth");
    }

    Criteria c = new TypeCriteria(UIAWebView.class);
    String json = c.stringify().toString();
    StringBuilder script = new StringBuilder();
    script.append("var root = UIAutomation.cache.get('1');");
    script.append("var webview = root.element(-1," + json + ");");
    script.append("var webviewSize = webview.rect();");
    script.append("var ratio = webviewSize.size.width / " + dim.getWidth() + ";");
    int top = po.getY();
    int left = po.getX();
    script.append("var top = (" + top + "*ratio )+1;");
    script.append("var left = (" + left + "*ratio)+1;");

    script.append("var x = left;");
    boolean ipad = session.getCapabilities().getDevice() == Device.ipad;
    if (ipad) {
      // for ipad, the adress bar h is fixed @ 96px.
      script.append("var y = top+96;");
    } else {
      AppleLocale current = session.getApplication().getCurrentLanguage();
      List<ContentResult>
          results =
          session.getApplication().getDictionary(current).getPotentialMatches("Address");
      if (results.size() != 1) {
        log.warning("translation returned " + results.size());
      }
      ContentResult result = results.get(0);
      String addressL10ned = result.getL10nFormatted();
      Criteria
          c2 =
          new AndCriteria(new TypeCriteria(UIAElement.class), new NameCriteria(addressL10ned),
                          new LabelCriteria(addressL10ned));
      script.append("var addressBar = root.element(-1," + c2.stringify().toString() + ");");
      script.append("var addressBarSize = addressBar.rect();");
      script.append("var delta = addressBarSize.origin.y +39;");
      script.append("if (delta<20){delta=20;};");
      script.append("var y = top+delta;");
    }
    script.append("UIATarget.localTarget().tap({'x':x,'y':y})");
    // script.append("var nativeElement = root.element(-1,{'x':x,'y':y});");
    // scroll into view.
    // script.append("nativeElement.isStale();");
    // script.append("nativeElement.tap();");

    return script.toString();

  }

  private String getNativeElementClickOnItAndTypeUsingKeyboardScript(String value)
      throws Exception {
    // web stuff.
    Point po = findPosition();
    Dimension dim = inspector.getSize();
    int webPageWidth = inspector.getInnerWidth();
    if (dim.getWidth() != webPageWidth) {
      log.warning("BUG : dim.getWidth()!=webPageWidth");
    }

    Criteria c = new TypeCriteria(UIAWebView.class);
    String json = c.stringify().toString();
    StringBuilder script = new StringBuilder();
    script.append("var root = UIAutomation.cache.get('1');");
    script.append("var webview = root.element(-1," + json + ");");
    script.append("var webviewSize = webview.rect();");
    script.append("var ratio = webviewSize.size.width / " + dim.getWidth() + ";");
    int top = po.getY();
    int left = po.getX();
    script.append("var top = (" + top + "*ratio )+1;");
    script.append("var left = (" + left + "*ratio)+1;");

    script.append("var x = left;");
    boolean ipad = session.getCapabilities().getDevice() == Device.ipad;
    if (ipad) {
      // for ipad, the adress bar h is fixed @ 96px.
      script.append("var y = top+96;");
    } else {
      AppleLocale current = session.getApplication().getCurrentLanguage();
      List<ContentResult>
          results =
          session.getApplication().getDictionary(current).getPotentialMatches("Address");
      if (results.size() != 1) {
        log.warning("translation returned " + results.size());
      }
      ContentResult result = results.get(0);
      String addressL10ned = result.getL10nFormatted();
      Criteria
          c2 =
          new AndCriteria(new TypeCriteria(UIAElement.class), new NameCriteria(addressL10ned),
                          new LabelCriteria(addressL10ned));
      script.append("var addressBar = root.element(-1," + c2.stringify().toString() + ");");
      script.append("var addressBarSize = addressBar.rect();");
      script.append("var delta = addressBarSize.origin.y +39;");
      script.append("if (delta<20){delta=20;};");
      script.append("var y = top+delta;");
    }

    script.append("var nativeElement = root.element(-1,{'x':x,'y':y});");
    // scroll into view.
    script.append("nativeElement.isStale();");
    // tap at the end of the element to try to append the text at the end.
    script.append("nativeElement.tap(0.99,0.5);");
    script.append("var keyboard = UIAutomation.cache.get('1').keyboard();");
    script.append("keyboard.typeString('" + value + "');");
    Criteria iPhone = new NameCriteria("Done");
    Criteria iPad = new NameCriteria("Hide keyboard");

    Criteria c3 = new OrCriteria(iPad, iPhone);
    // TODO freynaud create keyboard.hide();
    script.append("root.element(-1," + c3.stringify().toString() + ").tap();");

    return script.toString();

  }

  /*private UIAElement getNativeElement() throws Exception {
    // highlight();
    if (nativeElement == null) {

      // get the web element
      Point po = findPosition();
      int webPageWidth = inspector.getInnerWidth();
      // TODO freynaud use dim, remove innerw
      Dimension dim = inspector.getSize();

      WorkingMode origin = session.getMode();                                                                              ~
      UIARect rect = null;
      UIARect offset = null;

      session.setMode(WorkingMode.Native);
      UIAElement sv = nativeDriver.findElement(new TypeCriteria(UIAWebView.class));

      // scrollview container. Doesn't start in 0,0 // x=0,y=96,h=928w=768
      // TODO freynaud : should save the current value, and reset to that at
      // the end. Not to false.
      nativeDriver.configure(WebDriverLikeCommand.RECT).set("checkForStale", false);
      rect = sv.getRect();

      UIAElement addressBar = nativeDriver
          .findElement(
              new AndCriteria(new TypeCriteria(UIAElement.class), new NameCriteria("Address",
                                                                                   L10NStrategy.serverL10N),
                              new LabelCriteria("Address", L10NStrategy.serverL10N)));
      offset = addressBar.getRect();
      nativeDriver.configure(WebDriverLikeCommand.RECT).set("checkForStale", true);
      // rect = sv.getRect();

      int top = po.getY();
      int left = po.getX();

      float ratio = ((float) rect.getWidth()) / ((float) (webPageWidth));

      top = (int) (top * ratio) + 1;
      left = (int) (left * ratio) + 1;

      int statusBarHeigthIphone = 20;
      int x = left;

      int delta = offset.getY() + 39;
      // delta = heigth of the address bar + status bar.
      delta = delta < 20 ? 20 : delta;
      boolean ipad = session.getCapabilities().getDevice() == Device.ipad;
      if (ipad) {
        delta = 96;
      }
      int y = delta + top;

      nativeElement = nativeDriver.findElement(new LocationCriteria(x, y));


    }
    return nativeElement;
  } */

  public void setValueNative(String value) throws Exception {
    /*
     * WebElement el = getNativeElement(); WorkingMode origin =
     * session.getMode(); try { session.setMode(WorkingMode.Native); el.click();
     * RemoteUIAKeyboard keyboard = (RemoteUIAKeyboard)
     * session.getNativeDriver().getKeyboard(); if ("\n".equals(value)) {
     * keyboard.findElement(new NameCriteria("Return")).tap(); } else {
     * keyboard.sendKeys(value); }
     *
     * Criteria iphone = new NameCriteria("Done"); Criteria ipad = new
     * NameCriteria("Hide keyboard");
     *
     * UIAButton but = session.getNativeDriver().findElement(new
     * OrCriteria(ipad, iphone)); but.tap(); //
     * session.getNativeDriver().pinchClose(300, 400, 50, 100, 1); } finally {
     * session.setMode(origin); }
     */

    ((JavascriptExecutor) nativeDriver)
        .executeScript(getNativeElementClickOnItAndTypeUsingKeyboardScript(value));
  }
}