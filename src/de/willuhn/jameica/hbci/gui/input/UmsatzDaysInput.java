/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.Date;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.ScaleInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Auswahlfeld fuer die Anzahl der anzuzeigenden Tage.
 */
public class UmsatzDaysInput extends ScaleInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Settings settings = new Settings(UmsatzDaysInput.class);
  
  private final static String TOKEN_DEFAULT = "days";
  private Listener listener = new RangeListener();
  private String token = null;
  private Control c = null;

  /**
   * ct.
   */
  public UmsatzDaysInput()
  {
    super(0); 
    this.setValue(getDefaultDays() == -1 ? getUndefinedMax() : getDefaultDays()); // wir muessen das "-1" wieder zurueck auf 1000 mappen
    this.setName(i18n.tr("Zeitraum"));
    this.setComment(""); // Damit wir das Datum noch hinzufuegen koennen
    this.setScaling(1,getUndefinedMax(),1,10);
    this.addListener(this.listener);
  }
  
  /**
   * Liefert den Max-Wert, ab dem ein Wert von -1 angenommen wird.
   * @return der Max-Wert.
   */
  protected int getUndefinedMax()
  {
    return 1000;
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.ScaleInput#getControl()
   */
  public Control getControl()
  {
    if (c != null)
      return c;
    
    this.c = super.getControl();
    this.c.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        settings.setAttribute(getToken(),(Integer) getValue());
      }
    });
    
    this.listener.handleEvent(null); // einmal initial ausloesen
    return this.c;
  }
  
  /**
   * Das Auswahlfeld kann sich den letzten Zeitraum merken.
   * Damit dann aber nicht auf allen Dialogen der gleiche zeitraum vorausgewaehlt ist,
   * kann man hier einen individuellen Freitext-Token uebergeben, der als Key fuer
   * das Speichern des zuletzt ausgewaehlten Zeitraumes verwendet wird. Ueberall dort,
   * wo also der gleiche Token verwendet wird, wird auch der gleiche Zeitraum
   * vorausgewaehlt. Der Text kann z.Bsp. "auswertungen" heissen. Wenn dieser
   * auf allen Dialogen der Auswertungen verwendet wird, wird dort dann auch ueberall
   * der gleiche Zeitraum vorausgewaehlt sein.
   * @param s der Restore-Token.
   */
  public void setRememberSelection(String s)
  {
    this.token = s;
    int value = settings.getInt(this.token,HBCIProperties.UMSATZ_DEFAULT_DAYS);
    this.setValue(value == -1 ? getUndefinedMax() : value);
  }
  
  /**
   * Liefert den Store-Token.
   * @return der Store-Token.
   */
  private String getToken()
  {
    return this.token != null ? this.token : TOKEN_DEFAULT;
  }

  /**
   * Ueberschrieben, damit wir "-1" fuer "Alle Umsaetze" liefern koennen.
   * @see de.willuhn.jameica.gui.input.ScaleInput#getValue()
   */
  public Object getValue()
  {
    int i = (Integer) super.getValue();
    if (i >= getUndefinedMax())
      return -1;
    return i;
  }

  /**
   * Liefert die Anzahl der standardmaessig anzuzeigenden Tage.
   * @return Standardmaessig anzuzeigende Tage.
   */
  public final static int getDefaultDays()
  {
    return settings.getInt(TOKEN_DEFAULT,HBCIProperties.UMSATZ_DEFAULT_DAYS);
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.ScaleInput#setValue(java.lang.Object)
   */
  @Override
  public void setValue(Object value)
  {
    super.setValue(value);
    this.listener.handleEvent(null);
  }
  
  /**
   * Hilfsklasse zum Aktualisieren des Kommentars hinter dem Zeitraum.
   */
  private class RangeListener implements Listener
  {
    public void handleEvent(Event event)
    {
      try
      {
        int start = ((Integer)getValue()).intValue();
        if (start == 1)
        {
          setComment(i18n.tr("seit gestern"));
        }
        else if (start == -1)
        {
          setComment(i18n.tr("Alle Ums�tze"));
        }
        else if (start > 0)
        {
          long d = start * 24l * 60l * 60l * 1000l;
          Date date = DateUtil.startOfDay(new Date(System.currentTimeMillis() - d));
          setComment(i18n.tr("ab {0} ({1} Tage)",HBCI.DATEFORMAT.format(date),Integer.toString(start)));
        }
        else
        {
          setComment("");
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to update comment",e);
      }
    }
  }
}
