/**----------------------------------------------------------------------------
 * Name       ScriptHighlighter.java
 * Purpose    This file is to provide functionality to highlight all the occurrences of text in text area 
 * Modification History
 * Usage      Highlight the occurrences of the word "public"
 *            JTextArea textComp = new JTextArea();
 *            highlight(textComp, "public");
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public class ScriptHighlighter
{
  private Color color = Color.yellow;

  public ScriptHighlighter(Color color)
  {
    this.color = color;
  }

  // Creates highlights around all occurrences of pattern in textComp
  public int highlight(JTextComponent textComp, String pattern)
  {
    // First remove all old highlights
    int pos = 0;
    int countHighlightedWord = 0;
    removeHighlights(textComp);

    try
    {
      Highlighter hilite = textComp.getHighlighter();
      Document doc = textComp.getDocument();
      String text = doc.getText(0, doc.getLength());

      // Search for pattern
      while((pos = text.indexOf(pattern, pos)) >= 0)
      {
        // Create highlighter using private painter and apply around pattern
        hilite.addHighlight(pos, pos + pattern.length(), myHighlightPainter);
        pos += pattern.length();
        countHighlightedWord++;
      }
    }
    catch(BadLocationException e)
    {
    }
    return countHighlightedWord;
  }

  // Removes only our private highlights
  public void removeHighlights(JTextComponent textComp)
  {
    //System.out.println("removeHighlights method called ...");
    Highlighter hilite = textComp.getHighlighter();
    Highlighter.Highlight[] hilites = hilite.getHighlights();

    for(int i = 0; i < hilites.length; i++)
    {
      if(hilites[i].getPainter() instanceof MyHighlightPainter)
      {
        hilite.removeHighlight(hilites[i]);
      }
    }
  }

  // An instance of the private subclass of the default highlight painter
  Highlighter.HighlightPainter myHighlightPainter = new MyHighlightPainter(color);

  // A private subclass of the default highlight painter
  class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter
  {
    public MyHighlightPainter(Color color)
    {
      super(color);
      //System.out.println("MyHighlightPainter Constructor called ...");
    }
  }

}
