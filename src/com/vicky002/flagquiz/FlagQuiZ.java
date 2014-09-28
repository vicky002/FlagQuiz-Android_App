
package com.vicky002.flagquiz;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class FlagQuiZ extends Activity 
{
   // String used when logging error messages
   private static final String TAG = "FlagQuizGame Activity";
   
   private List<String> fileNameList; 
   private List<String> quizCountriesList; 
   private Map<String, Boolean> regionsMap; 
   private String correctAnswer; 
   private int totalGuesses;
   private int correctAnswers; 
   private int guessRows; 
   private Random random; 
   private Handler handler; 
   private Animation shakeAnimation;
   
   private TextView answerTextView; 
   private TextView questionNumberTextView; 
   private ImageView flagImageView;
   private TableLayout buttonTableLayout; 
   
   
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState); 
      setContentView(R.layout.main);

      fileNameList = new ArrayList<String>(); 
      quizCountriesList = new ArrayList<String>(); 
      regionsMap = new HashMap<String, Boolean>(); 
      guessRows = 1; 
      random = new Random(); 
      handler = new Handler(); 

     
      shakeAnimation = 
         AnimationUtils.loadAnimation(this, R.anim.incorrect_shake); 
      shakeAnimation.setRepeatCount(3);  
      
      
      String[] regionNames = 
         getResources().getStringArray(R.array.regions_list);
      
    
      for (String region : regionNames )
         regionsMap.put(region, true);

    
      questionNumberTextView = 
         (TextView) findViewById(R.id.questionnumbertextView1);
      flagImageView = (ImageView) findViewById(R.id.flagimageView1);
      buttonTableLayout = 
         (TableLayout) findViewById(R.id.buttonTableLayout);
      answerTextView = (TextView) findViewById(R.id.answerTextview);

     
      questionNumberTextView.setText(
         getResources().getString(R.string.questions) + " 1 " + 
         getResources().getString(R.string.of) + " 10");

      resetQuiz(); // start a new quiz
   }  
   private void resetQuiz() 
   {      
     
      AssetManager assets = getAssets(); 
      fileNameList.clear(); 
      
      try 
      {
         Set<String> regions = regionsMap.keySet(); //
         for (String region : regions) 
         {
            if (regionsMap.get(region))
            {
             
               String[] paths = assets.list(region);

               for (String path : paths) 
                  fileNameList.add(path.replace(".png", ""));
            } 
         } 
      } 
      catch (IOException e) 
      {
         Log.e(TAG, "Error loading image file names", e);
      } 
      
      correctAnswers = 0; 
      totalGuesses = 0; 
      quizCountriesList.clear(); 
      
    
      int flagCounter = 1; 
      int numberOfFlags = fileNameList.size(); 

      while (flagCounter <= 10) 
      {
         int randomIndex = random.nextInt(numberOfFlags); // random index

         
         String fileName = fileNameList.get(randomIndex);
         
        
         if (!quizCountriesList.contains(fileName)) 
         {
            quizCountriesList.add(fileName); // add the file to the list
            ++flagCounter;
         } 
      } 

      loadNextFlag(); 
   } 
  
   private void loadNextFlag() 
   {
     
      String nextImageName = quizCountriesList.remove(0);
      correctAnswer = nextImageName; 

      answerTextView.setText("");

      
      questionNumberTextView.setText(
         getResources().getString(R.string.questions) + " " + 
         (correctAnswers + 1) + " " + 
         getResources().getString(R.string.of) + " 10");

      
      String region = 
         nextImageName.substring(0, nextImageName.indexOf('-'));

     
      AssetManager assets = getAssets();
      InputStream stream; 

      try
      {
        
         stream = assets.open(region + "/" + nextImageName + ".png");
         
       
         Drawable flag = Drawable.createFromStream(stream, nextImageName);
         flagImageView.setImageDrawable(flag);                       
      } // end try
      catch (IOException e)  
      {
         Log.e(TAG, "Error loading " + nextImageName, e);
      } // end catch

     
      for (int row = 0; row < buttonTableLayout.getChildCount(); ++row)
         ((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();

      Collections.shuffle(fileNameList); // shuffle file names

     
      int correct = fileNameList.indexOf(correctAnswer);
      fileNameList.add(fileNameList.remove(correct));

    
      LayoutInflater inflater = (LayoutInflater) getSystemService(
         Context.LAYOUT_INFLATER_SERVICE);

     
      for (int row = 0; row < guessRows; row++) 
      {
         TableRow currentTableRow = getTableRow(row);

       
         for (int column = 0; column < 3; column++) 
         {
            
            Button newGuessButton = 
               (Button) inflater.inflate(R.layout.guess_button, null);
      
          
            String fileName = fileNameList.get((row * 3) + column);
            newGuessButton.setText(getCountryName(fileName));
            
          
            newGuessButton.setOnClickListener(guessButtonListener);
            currentTableRow.addView(newGuessButton);
         } 
      } 
      
    
      int row = random.nextInt(guessRows); // pick random row
      int column = random.nextInt(3); // pick random column
      TableRow randomTableRow = getTableRow(row); // get the TableRow
      String countryName = getCountryName(correctAnswer);
      ((Button)randomTableRow.getChildAt(column)).setText(countryName);    
   } 

   
   private TableRow getTableRow(int row)
   {
      return (TableRow) buttonTableLayout.getChildAt(row);
   } // end method getTableRow
   
   
   private String getCountryName(String name)
   {
      return name.substring(name.indexOf('-') + 1).replace('_', ' ');
   } // end method getCountryName
   

   private void submitGuess(Button guessButton) 
   {
      String guess = guessButton.getText().toString();
      String answer = getCountryName(correctAnswer);
      ++totalGuesses; // increment the number of guesses the user has made
      
      // if the guess is correct
      if (guess.equals(answer)) 
      {
         ++correctAnswers; // increment the number of correct answers

         // display "Correct!" in green text
         answerTextView.setText(answer + "!");
         answerTextView.setTextColor(
            getResources().getColor(R.color.correct_answer));

         disableButtons(); // disable all answer Buttons
         
         // if the user has correctly identified 10 flags
         if (correctAnswers == 10) 
         {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.reset_quiz); // title bar string
            
            // set the AlertDialog's message to display game results
            builder.setMessage(String.format("%d %s, %.02f%% %s", 
               totalGuesses, getResources().getString(R.string.guesses), 
               (1000 / (double) totalGuesses), 
               getResources().getString(R.string.Correct)));

            builder.setCancelable(false); 
            
            // add "Reset Quiz" Button                              
            builder.setPositiveButton(R.string.reset_quiz,
               new DialogInterface.OnClickListener()                
               {                                                       
                  public void onClick(DialogInterface dialog, int id) 
                  {
                     resetQuiz();                                      
                  } // end method onClick                              
               } // end anonymous inner class
            ); // end call to setPositiveButton
            
            // create AlertDialog from the Builder
            AlertDialog resetDialog = builder.create();
            resetDialog.show(); // display the Dialog
         } // end if 
         else // answer is correct but quiz is not over 
         {
            // load the next flag after a 1-second delay
            handler.postDelayed(
               new Runnable()
               { 
                  @Override
                  public void run()
                  {
                     loadNextFlag();
                  }
               }, 1000); // 1000 milliseconds for 1-second delay
         } // end else
      } // end if
      else // guess was incorrect  
      {
         // play the animation
         flagImageView.startAnimation(shakeAnimation);

         // display "Incorrect!" in red 
         answerTextView.setText(R.string.Incorrect_answer);
         answerTextView.setTextColor(
            getResources().getColor(R.color.incorrect_answer));
         guessButton.setEnabled(false); // disable the incorrect answer
      } // end else
   } // end method submitGuess

   // utility method that disables all answer Buttons 
   private void disableButtons()
   {
      for (int row = 0; row < buttonTableLayout.getChildCount(); ++row)
      {
         TableRow tableRow = (TableRow) buttonTableLayout.getChildAt(row);
         for (int i = 0; i < tableRow.getChildCount(); ++i)
            tableRow.getChildAt(i).setEnabled(false);
      } // end outer for
   } // end method disableButtons
   
   // create constants for each menu id
   private final int CHOICES_MENU_ID = Menu.FIRST;
   private final int REGIONS_MENU_ID = Menu.FIRST + 1;

   // called when the user accesses the options menu
   @Override
   public boolean onCreateOptionsMenu(Menu menu)             
   {            
      super.onCreateOptionsMenu(menu);                        
                                                              
      // add two options to the menu - "Choices" and "Regions"
      menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.Choices);             
      menu.add(Menu.NONE, REGIONS_MENU_ID, Menu.NONE, R.string.regions);             
                                                              
      return true; // display the menu                        
   }  // end method onCreateOptionsMenu                       

   // called when the user selects an option from the menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
      // switch the menu id of the user-selected option
      switch (item.getItemId()) 
      {
         case CHOICES_MENU_ID:
            // create a list of the possible numbers of answer choices
            final String[] possibleChoices = 
               getResources().getStringArray(R.array.guesseslist);

            // create a new AlertDialog Builder and set its title
            AlertDialog.Builder choicesBuilder = 
               new AlertDialog.Builder(this);         
            choicesBuilder.setTitle(R.string.Choices);
         
            // add possibleChoices's items to the Dialog and set the 
            // behavior when one of the items is clicked
            choicesBuilder.setItems(R.array.guesseslist,                    
               new DialogInterface.OnClickListener()                    
               {                                                        
                  public void onClick(DialogInterface dialog, int item) 
                  {                                                     
                     // update guessRows to match the user's choice     
                     guessRows = Integer.parseInt(                      
                        possibleChoices[item].toString()) / 3;          
                     resetQuiz(); // reset the quiz                     
                  } // end method onClick                               
               } // end anonymous inner class
            );  // end call to setItems                             
         
            // create an AlertDialog from the Builder
            AlertDialog choicesDialog = choicesBuilder.create();
            choicesDialog.show(); // show the Dialog            
            return true; 

         case REGIONS_MENU_ID:
            // get array of world regions
            final String[] regionNames = 
               regionsMap.keySet().toArray(new String[regionsMap.size()]);
         
            // boolean array representing whether each region is enabled
            boolean[] regionsEnabled = new boolean[regionsMap.size()];
            for (int i = 0; i < regionsEnabled.length; ++i)
               regionsEnabled[i] = regionsMap.get(regionNames[i]);

            // create an AlertDialog Builder and set the dialog's title
            AlertDialog.Builder regionsBuilder =
               new AlertDialog.Builder(this);
            regionsBuilder.setTitle(R.string.regions);
            
            // replace _ with space in region names for display purposes
            String[] displayNames = new String[regionNames.length];
            for (int i = 0; i < regionNames.length; ++i)
               displayNames[i] = regionNames[i].replace('_', ' ');
         
            // add displayNames to the Dialog and set the behavior
            // when one of the items is clicked
            regionsBuilder.setMultiChoiceItems( 
               displayNames, regionsEnabled,
               new DialogInterface.OnMultiChoiceClickListener() 
               {
                  @Override
                  public void onClick(DialogInterface dialog, int which,
                     boolean isChecked) 
                  {
                     // include or exclude the clicked region 
                     // depending on whether or not it's checked
                     regionsMap.put(
                        regionNames[which].toString(), isChecked);
                  } // end method onClick
               } // end anonymous inner class
            ); // end call to setMultiChoiceItems
          
            // resets quiz when user presses the "Reset Quiz" Button
            regionsBuilder.setPositiveButton(R.string.reset_quiz,
               new DialogInterface.OnClickListener()
               {
                  @Override
                  public void onClick(DialogInterface dialog, int button)
                  {
                     resetQuiz(); // reset the quiz
                  } // end method onClick
               } // end anonymous inner class
            ); // end call to method setPositiveButton
                        
            // create a dialog from the Builder 
            AlertDialog regionsDialog = regionsBuilder.create();
            regionsDialog.show(); // display the Dialog
            return true;
      } // end switch

      return super.onOptionsItemSelected(item);
   } // end method onOptionsItemSelected

   // called when a guess Button is touched
   private OnClickListener guessButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         submitGuess((Button) v); // pass selected Button to submitGuess
      } // end method onClick
   }; // end answerButtonListener
} // end FlagQuizGame
     

