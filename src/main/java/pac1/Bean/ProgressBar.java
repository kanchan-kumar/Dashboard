package pac1.Bean;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressBar extends JDialog implements ActionListener
{
  private String className = "ProgressBar";
  private Runnable progressBarRunnable;
  private Thread progressBarThread;
  private boolean startProgress;
  private JLabel label = new JLabel();
  private long timer = 0;
  private String msg = "";
  private boolean checkThread = false;

  public ProgressBar(JFrame owner, long timer, String msg)
  {
    super(owner);
    this.timer = timer;
    this.msg = msg;

    initializeGUI();
  }

  public ProgressBar(JDialog owner, long timer, String msg , String blankMsg)
  {
    super(owner);
    this.timer = timer;
    this.msg = msg;

    initializeGUI();
  }
  private void initializeGUI()
  {
    Log.debugLog(className, "initializeGUI", "", "", "Method Called");
    calculateLocation(500, 110);
    label.setFont(new Font("Verdana", Font.PLAIN, 12));
    setResizable(false);
    setTitle("Please Wait...");
    setFocusable(true);
    setLayout(new BorderLayout(24,0));
    getContentPane().add(new JLabel(""), BorderLayout.WEST);
    getContentPane().add(label, BorderLayout.CENTER);

    JPanel tempPanel = new JPanel(new BorderLayout(20,6));
    final JProgressBar progressBar = new JProgressBar(0, 100);

    tempPanel.add(new JLabel(" "), BorderLayout.EAST);
    tempPanel.add(progressBar, BorderLayout.CENTER);
    tempPanel.add(new JLabel(" "), BorderLayout.WEST);

    getContentPane().add(tempPanel, BorderLayout.SOUTH);
    JPanel panelForButton = new JPanel();
    panelForButton.setLayout(new GridBagLayout());
    tempPanel.add(panelForButton, BorderLayout.SOUTH);

    JButton cancelButton = new JButton("Run in Background");
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.insets = new Insets(0, 0, 5, 0);
    panelForButton.add(cancelButton, gridBagConstraints);
    cancelButton.addActionListener(this);

    progressBarRunnable = new Thread()
    {
      public void run()
      {
        int i = 1;
        startProgress = true;
        long cur = System.currentTimeMillis();//get current time
        while (startProgress)
        {
          if (i <= 100)
          {
            i = i + 3;
          }
          else
          {
            i = 0;
          }
          progressBar.setValue(i);
          try
          {
            sleep(100);
          }
          catch(InterruptedException e)
          {
            Log.stackTraceLog(className, "run", "", "", "Exception in waiting for progessBar", e);
          }
          if((System.currentTimeMillis() - cur) >= (timer*1000) && (timer != -1))
          {
            Log.debugLog(className, "run", "", "", "Time-out in execution.Coming out of the thread");
            checkThread = true;
            break;
          }
        }
        if(checkThread)
        {
          setVisible(false);
        }
      }
    };
  }

  public void actionPerformed(ActionEvent e)
  {
    setVisible(false);
  }

  public void setLabelMsg(String msg)
  {
    label.setText(msg);
  }

  public void setVisible(boolean bool)
  {
    super.setVisible(bool);
    Log.debugLog(className, "setVisible", "", "", "Method Called. bool = " + bool);
    if(bool)
    {
      while((progressBarThread != null) && (progressBarThread.isAlive()))
      {
        startProgress = false;
        try
        {
          Thread.sleep(100);
        }
        catch(Exception e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      progressBarThread = null;
//      if(progressBarThread == null)
      progressBarThread = new Thread(progressBarRunnable, "ProgressBarThread");
      progressBarThread.start();
    }
    else
    {
      startProgress = false;
      if(progressBarThread != null)
      {
        if(checkThread)
        {
          JOptionPane.showMessageDialog(null, msg,"" , JOptionPane.INFORMATION_MESSAGE);
          checkThread = false;
        }
/*        if(progressBarThread.isAlive())
          progressBarThread.stop();
        progressBarThread = null;*/
      }
    }
  }

  private void calculateLocation(int width, int height)
  {
    Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(new Dimension(width, height));
    int locationx = (screendim.width - width) / 2;
    int locationy = (screendim.height - height) / 2;
    setLocation(locationx, locationy);
  }
  //To test individually
  public static void main(String[] args)
  {
    try
    {
      JFrame f = new JFrame("TestData");
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      ProgressBar progessBar = new ProgressBar(f, 5, "good bye");
      progessBar.setLabelMsg("Starting ...");
      progessBar.setVisible(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
