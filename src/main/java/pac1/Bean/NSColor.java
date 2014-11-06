//-------------------------------------------------------------------//
//
//  Name   : NSColor.java
//  Author : Neeraj
//  Purpose: For keeping color which are used in Applets and Reports.
// Notes  :
//     This file is kept in two places - realTimGraph/client and bean
//      Always make changes in client dir and commit.
//      Then copy to bean and uncomment package lines and commit.
//    None
//  Modification History:
//-------------------------------------------------------------------//

// Uncomment next two lines for bean dir.
package pac1.Bean;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class NSColor
{
  private static String className = "NSColor";
  public final static String HX_VALUE_RED = "0xff3300";
  public final static String HX_VALUE_GREEN = "0x00ff00";
  public final static String HX_VALUE_YELOW = "0xffff00";
  public static String userType = "";
  // currently we use it but in future we use color.ini configure files.
  // Given color values.
  // Colors are according to chartcolor.ini file so max. 126 color can be choose

  //public static String colorName[] = {"DARK_BROWN ", "DARK_BLUE", "ORANGE", "VERY_DARK_RED", "LIGHT_BLUE", "LIGHT_GREEN", "VERY_LIGHT_YELLOW", "VERY_DARK_GREEN", "DARK_GREEN", "LIGHT_GREEN", "VERY_LIGHT_GREEN", "VERY_DARK_CYAN", "Megenta", "DARK_RED", "VOILETE", "DARK_YELLOW", "PURPLE", "BLACK", "VERY_DARK_BROWN", "PINK", "LIGHT_PINK", "DARK_GREEN_RED", "DARK_BROWN_RED", "DARK_BLOOD_RED", "DARK_GREEN_BLACK", "DARK_BROWN_GREEN", "DARK_BROWN_MAX", "DARK_YELLOW_BROWN", "DARK_SAFERON", "DIRTY_YELLOW", "DIRTY_GREEN", "BLACK_BLUE", "BLACK_BROWN", "PINK_BROWN", "PINK_DARK_RED", "DARK_GREEN_BLUE", "SLATEE", "CHOCHOLATE", "SWEET_DARK_PINK", "DARK_NAVY_BLUE", "VERY_DARK_BLUE", "DARK_SLATEE_BLUE", "SWEET_SLATEE", "CARPET_BROWN", "LIGHT_CHOCHO", "JOJO_VOILET", "SEVER_SLATEE", "GREEN_SILVER", "SILVER_SLATEE", "MAGIC_DIRTY_PINK", "MAGIC_DIRTY_GREEN", "YELLOW_WITH_SAFERON", "LIGHT_SLATEE_BLUE", "LIGHT_PINK_SLATEE", "SILVER_SKY_BLUE", "LIGHT_GREEN_BLUE", "DARK_GREEN_BLUE", "SILVER_LIGHT_SLATEE", "DARK_SKY_BLUE", "DARK_SILVER_SKY_BLUE", "LIGHT_RED_BLUE", "MAGENTA_BLUE", "MAGENTA_PINK", "PINK_RED", "MID_GREEN_BLUE", "DARK_RED_WITH_GB", "HAVY_GREEN_BLUE", "LIGHT_RED_WITH_DARK_GB", "SILVER_MATALIC", "L_GB_GREEN", "MAT_YELLOW", "RED_WITH_L_GB", "BLUE_WITH_L_RG", "L_RG_BLUE", "L_GREEN_BLUE", "CUSTOM_GREEN1", "CUSTOM_GREEN2", "CUSTOM_GREEN3", "CUSTOM_BLUE1", "CUSTOM_BLUE2", "CUSTOM_BLUE3", "CUSTOM_BLUE4", "CUSTOM_GREEN4", "CUSTOM_GREEN5", "CUSTOM_GREEN6", "CUSTOM_BLUE5", "CUSTOM_BLUE6", "CUSTOM_BLUE7", "CUSTOM_BLUE8", "CUSTOM_BLUE9", "CUSTOM_GREEN7", "CUSTOM_BLUE10", "CUSTOM_BLUE11", "CUSTOM_BLUE12", "CUSTOM_BLUE13", "CUSTOM_BLUE14", "CUSTOM_BLUE15", "CUSTOM_BLUE16", "CUSTOM_GREEN8", "CUSTOM_GREEN9", "CUSTOM_GREEN10", "CUSTOM_GRAY1", "CUSTOM_VOILET1", "CUSTOM_BLUE17", "CUSTOM_VOILET2", "CUSTOM_RED1", "CUSTOM_PINK1", "CUSTOM_PINK2", "CUSTOM_PINK3", "CUSTOM_VOILET3", "CUSTOM_GREEN11", "CUSTOM_VOILET4", "CUSTOM_RED2", "CUSTOM_ORANGE1", "CUSTOM_RED3", "CUSTOM_YELLOW1", "CUSTOM_RED4", "CUSTOM_ORANGE2", "CUSTOM_PINK4", "CUSTOM_PINK5", "CUSTOM_PINK6", "CUSTOM_YELLOW2", "CUSTOM_YELLOW3", "CUSTOM_RED5", "CUSTOM_RED6", "CUSTOM_GREEN12", "GhostWhite", "WhiteSmoke", "gainsboro", "FloralWhite", "OldLace", "linen", "AntiqueWhite", "PapayaWhip", "PeachPuff", "cornsilk", "ivory", "LemonChiffon", "honeydew", "azure", "AliceBlue", "lavender", "MistyRose", "LightSlateGrey", "grey", "LightGray", "MidnightBlue", "CornflowerBlue", "DarkSlateBlue", "SlateBlue", "LightSlateBlue", "MediumBlue", "RoyalBlue", "DodgerBlue", "DeepSkyBlue", "SkyBlue", "SteelBlue", "LightSteelBlue", "LightBlue", "PaleTurquoise", "DarkTurquoise", "turquoise", "LightCyan", "CadetBlue", "MediumAquamarine", "aquamarine", "DarkGreen", "DarkOliveGreen", "DarkSeaGreen", "SeaGreen", "MediumSeaGreen", "LightSeaGreen ", "PaleGreen", "SpringGreen", "LawnGreen", "MediumSpringGreen", "GreenYellow", "LimeGreen", "YellowGreen", "ForestGreen", "OliveDrab", "DarkKhaki", "khaki", "PaleGoldenrod", "LightGoldenrodYellow", "LightYellow", "gold", "LightGoldenrod", "goldenrod", "DarkGoldenrod", "RosyBrown", "IndianRed", "sienna", "peru", "burlywood", "beige", "wheat", "SandyBrown", "tan", "chocolate", "brown", "DarkSalmon", "salmon", "LightSalmon", "orange", "DarkOrange", "coral", "LightCoral", "tomato", "OrangeRed", "HotPink", "DeepPink", "LightPink", "PaleVioletRed", "maroon", "VioletRed", "violet", "plum", "orchid", "MediumOrchid", "DarkViolet", "BlueViolet", "purple", "MediumPurple", "thistle", "snow3", "snow4", "seashell1", "seashell2", "seashell3", "seashell4", "AntiqueWhite1", "AntiqueWhite2", "AntiqueWhite3", "AntiqueWhite4", "bisque1", "bisque2", "bisque3", "bisque4", "PeachPuff1", "PeachPuff2", "PeachPuff3", "PeachPuff4 ", "NavajoWhite1", "NavajoWhite2", "NavajoWhite3", "NavajoWhite4", "LemonChiffon1", "LemonChiffon2", "LemonChiffon3", "LemonChiffon4", "cornsilk1", "cornsilk2", "cornsilk3", "cornsilk4", "ivory2", "ivory3", "ivory4", "honeydew1", "honeydew2", "honeydew3", "honeydew4", "LavenderBlush1", "LavenderBlush2", "LavenderBlush3", "LavenderBlush4", "MistyRose1", "MistyRose2", "MistyRose3", "MistyRose4", "azure1", "azure2", "azure3", "azure4", "SlateBlue1", "SlateBlue3", "SlateBlue4", "RoyalBlue1", "RoyalBlue3", "RoyalBlue4", "blue4", "DodgerBlue1", "DodgerBlue3", "DodgerBlue4", "SteelBlue1", "SteelBlue3", "SteelBlue4", "DeepSkyBlue1", "DeepSkyBlue2", "DeepSkyBlue3", "DeepSkyBlue4", "SkyBlue1", "SkyBlue2", "SkyBlue3", "SkyBlue4", "LightSkyBlue1", "LightSkyBlue2", "LightSkyBlue3", "LightSkyBlue4", "SlateGray2", "SlateGray3", "SlateGray4", "LightBlue1", "LightBlue2", "LightBlue3", "LightCyan1", "LightCyan2", "LightCyan3", "LightCyan4", "PaleTurquoise1", "PaleTurquoise2", "PaleTurquoise3", "PaleTurquoise4", "CadetBlue3", "CadetBlue4", "DarkSlateGray1", "DarkSlateGray2", "DarkSlateGray3", "DarkSlateGray4", "aquamarine1", "aquamarine2", "aquamarine4", "DarkSeaGreen1", "DarkSeaGreen2", "DarkSeaGreen3", "DarkSeaGreen4", "SeaGreen1", "SeaGreen2", "SeaGreen3", "SeaGreen4", "PaleGreen1", "PaleGreen2", "PaleGreen3", "PaleGreen4", "green2", "green3", "green4", "chartreuse1", "chartreuse2", "chartreuse3", "chartreuse4", "OliveDrab1", "OliveDrab2", "OliveDrab3", "OliveDrab4", "DarkOliveGreen1", "DarkOliveGreen2", "DarkOliveGreen3", "DarkOliveGreen4", "khaki1", "khaki2", "khaki3", "khaki4", "LightGoldenrod1", "LightGoldenrod2", "LightGoldenrod3", "LightGoldenrod4", "LightYellow2", "LightYellow3", "LightYellow4", "yellow2", "yellow3", "yellow4", "gold1", "gold2", "gold3", "gold4", "goldenrod1", "goldenrod2", "goldenrod3", "goldenrod4", "DarkGoldenrod1", "DarkGoldenrod2", "DarkGoldenrod3", "DarkGoldenrod4", "RosyBrown1", "RosyBrown2", "RosyBrown3", "RosyBrown4", "IndianRed1", "IndianRed2", "IndianRed3", "IndianRed4", "sienna1", "sienna2", "sienna3", "sienna4", "burlywood1", "burlywood4", "wheat3", "wheat4", "tan1", "tan2", "tan3", "tan4", "chocolate1", "chocolate2", "chocolate3", "chocolate4", "firebrick1", "firebrick2", "firebrick3", "firebrick4", "brown1", "brown2", "brown3", "brown4", "salmon1", "salmon2", "salmon3", "salmon4", "LightSalmon2", "LightSalmon3", "LightSalmon4", "orange1", "orange2", "orange3", "orange4", "DarkOrange1", "DarkOrange2", "DarkOrange3", "DarkOrange4", "coral1", "coral2", "coral3", "coral4", "tomato2", "tomato3", "tomato4", "OrangeRed2", "OrangeRed3", "OrangeRed4", "red2", "red3", "red4", "DeepPink1", "DeepPink2", "DeepPink3", "DeepPink4", "HotPink1", "HotPink2", "HotPink3", "HotPink4", "pink1", "pink2", "pink3", "pink4", "LightPink1", "LightPink2", "LightPink3", "LightPink4"};
  //public static int[] r = { 0, 255, 102, 204, 255, 0, 0, 128, 0, 205, 51, 160, 255, 132, 0, 255, 165, 51, 155, 0, 255, 102, 102, 153, 0, 51, 102, 153, 204, 204, 204, 0, 51, 102, 153, 0, 51, 102, 153, 0, 51, 102, 102, 153, 204, 102, 102, 102, 153, 204, 204, 255, 153, 204, 204, 153, 0, 204, 0, 51, 102, 153, 204, 255, 0, 255, 0, 153, 204, 204, 255, 204, 153, 102, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 51, 51, 102, 51, 102, 153, 153, 153, 153, 153, 204, 204, 204, 204, 204, 204, 204, 204, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 204, 248, 245, 220, 255, 253, 250, 250, 255, 255, 255, 255, 255, 240, 240, 240, 230, 255, 119, 190, 211, 25, 100, 72, 106, 132, 0, 65, 30, 0, 135, 70, 176, 173, 175, 0, 64, 224, 95, 102, 127, 0, 85, 143, 46, 60, 32, 152, 0, 124, 0, 173, 50, 154, 34, 107, 189, 240, 238, 250, 255, 255, 238, 218, 184, 188, 205, 160, 205, 222, 245, 245, 244, 210, 210, 165, 233, 250, 255, 255, 255, 255, 240, 255, 255, 255, 255, 255, 219, 146, 208, 238, 221, 218, 186, 148, 138, 160, 147, 216, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 238, 205, 139, 240, 224, 193, 131, 255, 238, 205, 139, 225, 238, 205, 139, 240, 224, 193, 131, 131, 105, 71, 72, 58, 39, 0, 30, 24, 16, 99, 79, 54, 0, 0, 0, 0, 135, 126, 108, 74, 176, 164, 141, 96, 185, 159, 108, 191, 178, 154, 224, 209, 180, 122, 187, 174, 150, 102, 122, 83, 151, 141, 121, 82, 127, 118, 69, 193, 180, 155, 105, 84, 78, 67, 46, 154, 144, 124, 84, 0, 0, 0, 127, 118, 102, 69, 192, 179, 154, 105, 202, 188, 162, 110, 255, 238, 205, 139, 255, 238, 205, 139, 238, 205, 139, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 139, 205, 139, 255, 238, 205, 139, 225, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 238, 205, 139, 238, 205, 139, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139 };
  //public static int[] g = { 0, 102, 0, 153, 0, 128, 153, 0, 128, 91, 204, 82, 40, 66, 0, 99, 42, 51, 0, 192, 204, 102, 0, 0, 51, 51, 51, 102, 102, 153, 204, 0, 0, 0, 0, 51, 51, 51, 0, 51, 51, 51, 102, 102, 153, 51, 102, 153, 153, 153, 204, 204, 153, 153, 255, 255, 255, 204, 0, 0, 0, 0, 0, 0, 153, 153, 204, 204, 204, 255, 255, 102, 102, 102, 102, 153, 153, 153, 0, 0, 153, 153, 204, 204, 204, 51, 51, 204, 51, 204, 255, 102, 255, 102, 204, 102, 255, 204, 255, 255, 153, 153, 0, 153, 51, 0, 0, 0, 0, 51, 255, 102, 102, 102, 102, 153, 0, 153, 0, 153, 204, 255, 255, 51, 51, 255, 248, 245, 220, 250, 245, 240, 235, 239, 218, 248, 255, 250, 255, 255, 248, 230, 228, 136, 190, 211, 25, 149, 61, 190, 112, 0, 105, 144, 191, 206, 130, 196, 216, 238, 206, 224, 255, 158, 205, 255, 100, 107, 188, 139, 179, 178, 251, 255, 252, 250, 255, 205, 205, 139, 142, 183, 230, 232, 250, 255, 215, 221, 165, 134, 143, 92, 82, 133, 184, 245, 222, 164, 180, 105, 42, 150, 128, 160, 165, 140, 127, 128, 99, 69, 105, 20, 182, 112, 48, 32, 130, 160, 112, 85, 0, 43, 32, 112, 119, 201, 137, 245, 229, 197, 134, 239, 223, 192, 131, 238, 213, 183, 125, 218, 203, 175, 119, 222, 207, 179, 121, 250, 233, 201, 137, 248, 232, 200, 136, 238, 205, 139, 255, 238, 205, 139, 240, 224, 193, 131, 228, 213, 183, 125, 255, 238, 205, 139, 111, 89, 60, 118, 95, 64, 0, 144, 116, 78, 148, 148, 100, 191, 178, 154, 104, 206, 192, 166, 112, 226, 211, 182, 123, 211, 182, 123, 239, 223, 192, 255, 238, 205, 139, 255, 238, 205, 139, 197, 134, 255, 238, 205, 139, 255, 238, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 246, 230, 198, 134, 236, 220, 190, 129, 238, 205, 139, 238, 205, 139, 215, 201, 173, 117, 193, 180, 155, 105, 185, 173, 149, 101, 193, 180, 155, 105, 106, 99, 85, 58, 130, 121, 104, 71, 211, 115, 186, 126, 165, 154, 133, 90, 127, 118, 102, 69, 48, 44, 38, 26, 64, 59, 51, 35, 140, 130, 112, 76, 149, 129, 87, 165, 154, 133, 90, 127, 118, 102, 69, 114, 106, 91, 62, 92, 79, 54, 64, 55, 37, 0, 0, 0, 20, 18, 16, 10, 110, 106, 96, 58, 181, 169, 145, 99, 174, 162, 140, 95 };
  //public static int[] b = { 0, 0, 204, 0, 0, 128, 204, 0, 0, 69, 51, 45, 80, 0, 102, 71, 42, 0, 155, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 51, 51, 51, 51, 51, 51, 102, 102, 102, 102, 102, 102, 102, 153, 153, 153, 153, 153, 153, 153, 204, 204, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 153, 153, 153, 204, 204, 204, 204, 204, 204, 204, 204, 0, 51, 102, 153, 204, 204, 255, 0, 51, 102, 153, 204, 204, 255, 255, 0, 153, 204, 255, 255, 102, 255, 255, 153, 51, 0, 102, 153, 255, 255, 0, 102, 153, 204, 255, 51, 255, 102, 51, 102, 0, 102, 102, 204, 204, 204, 0, 153, 102, 153, 153, 255, 255, 220, 240, 230, 230, 215, 213, 185, 220, 240, 205, 240, 255, 255, 250, 225, 153, 190, 211, 112, 237, 139, 205, 255, 205, 255, 255, 255, 235, 180, 222, 230, 238, 209, 208, 255, 160, 170, 212, 0, 47, 143, 87, 113, 170, 152, 127, 0, 154, 47, 50, 50, 34, 35, 107, 140, 170, 210, 224, 0, 130, 32, 11, 143, 92, 45, 63, 135, 220, 179, 96, 140, 30, 42, 122, 114, 122, 0, 0, 80, 128, 71, 0, 180, 147, 193, 147, 96, 144, 238, 221, 214, 211, 211, 226, 240, 219, 216, 201, 137, 238, 222, 191, 130, 219, 204, 176, 120, 196, 183, 158, 107, 185, 173, 149, 101, 173, 161, 139, 94, 205, 191, 165, 112, 220, 205, 177, 120, 224, 193, 131, 240, 224, 193, 131, 245, 229, 197, 134, 225, 210, 181, 123, 255, 238, 205, 139, 255, 205, 139, 255, 205, 139, 139, 255, 205, 139, 255, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 255, 238, 205, 139, 238, 205, 139, 255, 238, 205, 255, 238, 205, 139, 255, 238, 205, 139, 205, 139, 255, 238, 205, 139, 212, 198, 116, 193, 180, 155, 105, 159, 148, 128, 87, 154, 144, 124, 84, 0, 0, 0, 0, 0, 0, 0, 62, 58, 50, 34, 112, 104, 90, 61, 143, 133, 115, 78, 139, 130, 112, 76, 209, 180, 122, 0, 0, 0, 0, 0, 0, 0, 37, 34, 29, 20, 15, 14, 12, 8, 193, 180, 155, 105, 106, 99, 85, 58, 71, 66, 57, 38, 155, 85, 105, 102, 79, 73, 63, 43, 36, 38, 29, 19, 48, 44, 38, 26, 64, 59, 51, 35, 105, 98, 84, 57, 114, 98, 66, 0, 0, 0, 0, 0, 0, 0, 0, 86, 80, 69, 47, 66, 57, 38, 0, 0, 0, 0, 0, 0, 147, 137, 118, 80, 180, 167, 144, 98, 197, 184, 158, 108, 185, 173, 149, 101 };

  public static String colorName[] = {"Black", "Custom_Orange2", "Purple", "Dirty_Yellow", "Dark_Red", "Very_Dark_Cyan", "Custom_Blue33", "Very_Dark_Red", "Dark_Green", "Coral4", "Light_Green2", "Sienna", "Ivory2", "Dark_Brown", "Dark_Blue", "Tomato", "Brown", "Dark_Brown_Green", "Megenta", "Light_Green", "Dark_Yellow", "Light_Purple", "Custom_Blue29", "Carpet_Brown", "Very_Dark_Brown", "Pink", "Very_Dark_Blue", "Dark_Green_Red", "Light_Red_Blue", "Orange", "Dark_Brown_Red", "Deep_Sky_Blue", "Dark_Green_Black", "Light_Green1", "Dark_Brown_Max", "Sever_Slatee", "Dark_Yellow_Brown", "Dark_Blood_Red", "Dark_Saferon", "Custom_Yellow3", "Black_Blue", "Coral2", "Robin_Blue", "Black_Brown", "Spring_Green", "Pink_Dark_Red", "Slate_Blue", "Light_Coral", "Magenta_Pink", "Dark_Green_Blue1", "Light_Red_With_Dark_Gb", "Slatee", "Light_Chocho", "Chocholate", "Light_Pink_Slatee", "Sweet_Dark_Pink", "Custom_Blue27", "Custom_Yellow1", "Jojo_Violet", "Light_Pink1", "Havy_Green_Blue", "Custom_Red2", "Dark_Navy_Blue", "Custom_Green8", "Dark_Slatee_Blue", "Sweet_Slatee", "Custom_Blue31", "Pink_Brown", "Dark_Olive_Green1", "Light_Slatee_Blue", "Magenta_Blue", "Custom_Pink1", "Custom_Blue32", "Mid_Green_Blue", "Violet", "Custom_Orange3", "Dark_Green_Blue", "Blue", "Custom_Red3", "Custom_Green7", "Custom_Pink3", "L_Rg_Blue", "Custom_Green10", "Dark_Silver_Sky_Blue", "Deep_Pink2", "Custom_Green12", "Custom_Violet6", "Orange_Red", "Medium_Blue", "Pink_Red", "Yellow_Green", "Custom_Yellow2", "Red_With_L_Gb", "Chocolate3", "Deep_Pink1", "Blue_With_L_Rg", "L_Green_Blue", "Custom_Green3", "Indian_Red", "Custom_Gray1", "Custom_Blue23", "Custom_Pink9", "Custom_Blue34", "Custom_Green9", "Custom_Violet5", "Fire_Brick3", "Custom_Blue36", "Custom_Blue39", "Custom_Red5", "Custom_Green11", "Custom_Blue35", "Goldenrod", "Custom_Blue37", "Custom_Red4", "Sea_Green", "Blue_Violet", "Custom_Pink8", "Custom_Blue38", "Custom_Green13", "Custom_Red1", "Custom_Green5", "Custom_Pink6", "Custom_Blue24", "Custom_Blue25", "Dark_Orange", "Light_Pink", "Lime_Green", "Custom_Blue26", "Custom_Orange6", "Custom_Red6", "Custom_Blue28", "Custom_Blue30", "Custom_Green4", "Midnight_Blue", "Burlywood", "Custom_Violet2", "Custom_Green14", "Custom_Violet3", "Custom_Orange1", "Hot_Pink4", "Custom_Pink12", "Dark_Green1", "Dark_Golden_Rod", "Medium_Orchid", "Dodger_Blue", "Lawn_Green", "Light_Slate_Gray", "Custom_Red12", "Dark_Golden_Rod1", "Custom_Blue8", "Custom_Green2", "Light_Gray", "Peach", "Dark_Slate_Blue", "Yellow_Green2", "Dark_Olive_Green", "Dodger_Blue1", "Dark_Pink", "Forest_Green", "Steel_Blue", "Silver_Slatee", "Custom_Purple1", "Custom_Yellow6", "Dark_Turguoise", "Magic_Dirty_Pink", "Olive_Drab", "Hot_Pink", "Medium_Se", "Custom_Yellow4", "Custom_Brown2", "Chocolate1", "Custom_Blue5", "Turguoise", "Green_Yellow", "Peru", "Violet_Red", "Cadet_Blue", "Dark_Salmon1", "Chartreuse1", "Dark_Violet", "Rosy_Brown", "Markt_Blue", "Paleturquoise", "Custom_Blue3", "Mediumaquamarine", "Aquamarine", "Custom_Pink4", "Lightseagreen", "Palegreen", "Mediumspringgreen", "Sky_Blue1", "Khaki", "Custom_Violet1", "Peach1", "Custom_Blue15", "Custom_Green1", "Rosy_Brown1", "Beige", "Wheat", "Sandybrown", "Tan", "Salmon", "Custom_Blue11", "Lightpink", "Plum", "Orchid", "Medium_Purple", "Thistle", "Snow3", "Snow4", "Custom_Violet4", "Seashell2", "Seashell3", "Palevioletred", "Seashell4", "Antiquewhite1", "Antiquewhite2", "Custom_Brown1", "Antiquewhite4", "Bisque1", "Bisque2", "Bisque3", "Bisque4", "Peachpuff1", "Peachpuff2", "Peachpuff3", "Peachpuff4", "Darkorange1", "Navajowhite2", "Custom_Green18", "Navajowhite4", "Lemonchiffon1", "Lemonchiffon2", "Custom_Green24", "Lemonchiffon4", "Cornsilk1", "Cornsilk2", "Custom_Pink13", "Cornsilk4", "Ivory3", "Ivory4", "Honeydew1", "Honeydew2", "Honeydew3", "Honeydew4", "Mehroom1", "Lavenderblush2", "Custom_Blue14", "Lavenderblush4", "Mistyrose1", "Mistyrose2", "Mistyrose3", "Mistyrose4", "Azure1", "Azure2", "Custom_Red15", "Azure4", "Custom_Green23", "Slate_Blue1", "Slate_Blue2", "Royalblue1", "Royalblue3", "Royalblue4", "Dodgerblue1", "Custom_Green27", "Steel_Blue1", "Steel_Blue2", "Steel_Blue3", "Deepskyblue1", "Deepskyblue3", "Deepskyblue4", "Sky_Blue2", "Sky_Blue3", "Lemon1", "Sky_Blue4", "Lightskyblue1", "Lightskyblue2", "Custom_Green31", "Lightskyblue4", "Deepskyblue2", "Custom_Blue18", "Custom_Red13", "Slategray4", "Custom_Green33", "Lightblue2", "Custom_Blue21", "Lightcyan1", "Custom_Brown3", "Lightcyan3", "Lightcyan4", "Paleturquoise1", "Paleturquoise2", "Paleturquoise3", "Paleturquoise4", "Custom_Pink14", "Cadetblue4", "Darkslategray1", "Darkslategray2", "Custom_Green16", "Light_Brown1", "Custom_Red7", "Aquamarine2", "Darkseagreen2", "Lightviolet1", "Darkseagreen3", "Sea_Green2", "Custom_Pink10", "Sea_Green3", "Sea_Green4", "Palegreen1", "Custom_Red8", "Palegreen3", "Palegreen4", "Custom_Green25", "Custom_Blue7", "Custom_Green29", "Custom_Blue12", "Custom_Red11", "Custom_Green22", "Olivedrab1", "Custom_Purple3", "Olivedrab3", "Custom_Purple4", "Custom_Yellow12", "Custom_Blue2", "Darkolivegreen3", "Darkolivegreen4", "Dark_Mehroom", "Custom_Blue1", "Khaki3", "Khaki4", "Lightgoldenrod1", "Lightgoldenrod2", "Lightgoldenrod3", "Lightgoldenrod4", "Custom_Green19", "Custom_Orange7", "Custom_Slatee1", "Custom_Yellow8", "Custom_Green28", "Aquamarine4", "Darkseagreen1", "Custom_Green30", "Custom_Yellow11", "Custom_Blue19", "Custom_Green34", "Custom_Green35", "Custom_Blue20", "Custom_Green36", "Goldenrod3", "Goldenrod4", "Darkgoldenrod1", "Darkgoldenrod3", "Darkgoldenrod4", "Violet_Blue", "Custom_Blue22", "Rosy_Brown2", "Indianred1", "Custom_Green32", "Custom_Green38", "Indianred4", "Sienna1", "Sienna2", "Sienna3", "Sienna4", "Burlywood1", "Burlywood4", "Wheat3", "Wheat4", "Tan1", "Tan2", "Tan3", "Tan4", "Lemon", "Chocolate2", "Chocolate4", "Firebrick1", "Firebrick2", "Firebrick4", "Custom_Green15", "Custom_Blue4", "Custom_Yellow5", "Custom_Brown4", "Salmon1", "Salmon2", "Salmon3", "Salmon4", "Custom_Brown5", "Custom_Red16", "Lightsalmon4", "Custom_Orange5", "Custom_Green17", "Custom_Orange8", "Custom_Purple2", "Darkorange2", "Custom_Green26", "Custom_Blue13", "Light_Brown2", "Coral1", "Coral3", "Coral5", "Tomato2", "Tomato3", "Tomato4", "Orangered2", "Custom_Green37", "Orangered4", "Custom_Red10", "Custom_Red14", "Deeppink1", "Custom_Yellow10", "Deeppink2", "Hotpink1", "Custom_Blue16", "Hotpink3", "Custom_Pink7", "Custom_Pink11", "Custom_Yellow7", "Custom_Slatee3", "Yellow_Green1", "Lightpink2", "Custom_Green20", "Custom_Slatee2", "Custom_Blue10", "Custom_Red9", "Custom_Blue6", "Custom_Yellow13", "L_Gb_Green", "Custom_Pink5", "Custom_Blue9", "Custom_Blue17", "Gainsboro", "Mehendi", "Oldlace", "Linen", "Custom_Pink2", "Papayawhip", "Cornsilk", "Custom_Orange4", "Ivory", "Blue_Green", "Honeydew", "Azure", "Aliceblue", "Custom_Green21", "Mistyrose", "Pink_Blue", "Sea_Green1", "Custom_Green6", "Custom_Yellow9", "Yellow_With_Saferon"};
  public static int[] r = { 0, 255, 102, 204, 255,   0, 128, 255,   0, 205,  51, 160,  132,   0, 255, 165, 51, 155, 0, 255, 204, 51, 153, 51, 255, 51, 102, 102, 255, 102, 0, 0, 64, 102, 102, 153, 153, 204, 204, 0, 255, 75, 51, 0, 153, 106, 240, 204, 0, 153, 51, 204, 102, 204, 153, 51, 255, 102, 255, 0, 204, 0, 0, 102, 102, 153, 102, 85, 153, 153, 204, 0, 0, 153, 255, 0, 0, 255, 0, 204, 102, 0, 51, 238, 0, 204, 255, 0, 255, 154, 255, 204, 205, 255, 153, 0, 0, 205, 153, 0, 255, 0, 0, 204, 205, 0, 0, 255, 0, 0, 218, 0, 255, 46, 138, 255, 0, 51, 204, 204, 204, 0, 0, 255, 238, 50, 0, 228, 255, 51, 102, 153, 25, 138, 153, 102, 153, 255, 139, 0, 0, 184, 186, 24, 124, 119, 205, 238, 60, 0, 157, 100, 72, 204, 79, 30, 192, 34, 70, 153, 160, 213, 0, 172, 107, 255, 60, 255, 146, 210, 0, 64, 173, 205, 208, 95, 233, 127, 148, 139, 29, 211, 0, 102, 127, 204, 32, 152, 0, 83, 166, 165, 238, 64, 67, 188, 34, 88, 244, 36, 250, 0, 255, 60, 95, 147, 216, 7, 139, 255, 165, 73, 219, 139, 255, 167, 116, 139, 255, 136, 53, 139, 223, 16, 205, 139, 180, 215, 65, 139, 151, 59, 0, 139, 202, 140, 184, 139, 54, 139, 87, 47, 230, 143, 119, 64, 0, 139, 55, 195, 78, 139, 87, 82, 186, 131, 30, 71, 119, 255, 58, 221, 30, 20, 99, 70, 45, 0, 32, 186, 1, 12, 246, 74, 78, 26, 70, 96, 0, 35, 239, 108, 100, 97, 50, 147, 151, 180, 122, 0, 149, 255, 102, 170, 83, 243, 0, 40, 172, 180, 20, 45, 156, 105, 84, 246, 67, 46, 181, 199, 124, 84, 0, 20, 0, 90, 254, 69, 192, 96, 154, 165, 235, 121, 162, 110, 100, 38, 163, 139, 197, 225, 23, 139, 83, 244, 139, 238, 104, 69, 113, 139, 255, 110, 46, 139, 25, 51, 205, 139, 195, 174, 229, 152, 38, 81, 204, 82, 3, 139, 255, 128, 235, 139, 255, 139, 123, 172, 255, 200, 205, 139, 255, 238, 139, 255, 77, 139, 90, 0, 240, 139, 56, 238, 100, 152, 123, 210, 139, 255, 23, 205, 160, 255, 23, 30, 139, 255, 83, 139, 238, 153, 201, 238, 166, 139, 238, 139, 255, 219, 139, 255, 41, 205, 255, 180, 255, 139, 158, 221, 84, 139, 0, 200, 25, 200, 0, 216, 125, 1, 90, 84, 0, 129, 150, 255, 53, 204, 136, 255, 81, 121, 0, 154, 126, 255, 21, 115, 255, 255 };
  public static int[] g = { 0, 102,   0, 153,   0, 128,   0,  40, 153,  91, 204,  82,   66,   0,  99,  42,  51, 0, 192, 204, 153, 255, 102, 0, 51, 51, 102, 0, 165, 0, 191, 51, 255, 51, 102, 102, 0, 102, 204, 0, 127, 75, 0, 255, 0, 190, 128, 0, 51, 204, 51, 153, 51, 153, 0, 204, 153, 51, 153, 204, 102, 51, 153, 51, 102, 153, 0, 107, 153, 0, 0, 0, 153, 0, 153, 255, 0, 102, 153, 0, 102, 204, 0, 18, 255, 102, 69, 0, 0, 205, 255, 102, 102, 20, 102, 102, 153, 92, 153, 0, 153, 153, 204, 51, 38, 51, 204, 51, 204, 51, 165, 204, 0, 139, 43, 0, 51, 255, 0, 255, 0, 102, 255, 140, 130, 205, 102, 109, 51, 102, 204, 153, 25, 95, 0, 255, 51, 102, 58, 102, 100, 134, 85, 116, 252, 136, 0, 173, 86, 146, 157, 149, 61, 155, 98, 144, 34, 139, 130, 153, 32, 209, 206, 88, 142, 105, 179, 215, 48, 105, 0, 224, 255, 133, 32, 158, 150, 255, 0, 105, 158, 216, 39, 205, 255, 0, 178, 251, 250, 142, 149, 9, 119, 50, 193, 143, 35, 61, 164, 45, 128, 139, 182, 51, 235, 112, 119, 131, 137, 245, 119, 64, 112, 134, 161, 116, 44, 131, 192, 89, 41, 125, 218, 198, 175, 119, 34, 144, 191, 121, 72, 65, 176, 137, 165, 122, 38, 136, 15, 139, 255, 95, 0, 149, 19, 200, 72, 131, 80, 106, 171, 125, 255, 150, 6, 139, 100, 53, 123, 25, 95, 224, 144, 160, 148, 17, 155, 108, 55, 112, 152, 57, 246, 112, 76, 98, 150, 123, 178, 90, 45, 123, 120, 58, 80, 255, 72, 205, 139, 68, 55, 255, 139, 60, 134, 116, 154, 180, 128, 40, 160, 201, 129, 139, 255, 24, 205, 139, 95, 65, 205, 139, 238, 50, 139, 78, 36, 139, 255, 73, 205, 9, 230, 105, 205, 139, 30, 20, 45, 134, 91, 193, 67, 129, 166, 132, 139, 238, 102, 139, 255, 139, 215, 77, 88, 117, 9, 221, 155, 105, 106, 44, 127, 142, 54, 41, 42, 80, 63, 58, 130, 224, 240, 71, 180, 115, 14, 40, 165, 0, 133, 90, 255, 118, 69, 48, 165, 26, 130, 90, 180, 35, 18, 130, 81, 44, 46, 0, 87, 165, 154, 133, 40, 127, 197, 140, 69, 53, 36, 62, 92, 55, 226, 64, 163, 37, 0, 0, 20, 214, 10, 110, 57, 96, 47, 0, 250, 99, 154, 67, 233, 95, 90, 30, 25, 150, 46, 52, 125, 1, 90, 64, 104, 77, 0, 161, 82, 97, 206, 250, 35, 91, 226, 117, 0, 21, 255, 255, 255, 130 };
  public static int[] b = { 0,   0, 204,   0,   0, 128,   0,  80, 204,  69,  51,  45,    0, 102,  71,  42,  0, 155, 0, 0, 255, 255, 102, 0, 255, 102, 0, 255, 0, 0, 255, 0, 64, 0, 153, 0, 0, 0, 0, 51, 80, 220, 51, 127, 51, 205, 128, 255, 51, 204, 51, 102, 51, 204, 102, 255, 0, 153, 153, 153, 102, 102, 102, 102, 102, 255, 51, 47, 204, 255, 102, 204, 153, 204, 102, 255, 255, 102, 51, 153, 204, 51, 255, 137, 0, 255, 0, 205, 255, 50, 0, 204, 29, 147, 204, 204, 0, 92, 102, 153, 204, 255, 0, 255, 38, 204, 255, 102, 102, 153, 32, 204, 102, 87, 226, 204, 255, 153, 0, 51, 204, 153, 204, 0, 238, 50, 255, 10, 153, 102, 255, 0, 112, 38, 153, 51, 255, 51, 98, 255, 0, 11, 211, 205, 0, 153, 0, 14, 216, 0, 157, 237, 139, 0, 40, 255, 169, 34, 180, 153, 240, 47, 209, 88, 35, 180, 113, 0, 96, 30, 139, 208, 47, 63, 144, 160, 122, 0, 211, 105, 255, 22, 118, 170, 212, 102, 170, 152, 154, 213, 21, 124, 0, 100, 82, 143, 78, 12, 96, 35, 114, 234, 193, 79, 142, 219, 216, 90, 137, 238, 85, 87, 147, 130, 47, 55, 42, 120, 33, 32, 27, 107, 0, 16, 149, 101, 0, 39, 89, 94, 7, 51, 80, 112, 0, 48, 163, 120, 195, 131, 87, 191, 0, 61, 69, 64, 126, 134, 42, 120, 63, 123, 255, 148, 6, 139, 50, 177, 77, 0, 205, 36, 255, 20, 255, 205, 39, 146, 117, 56, 255, 88, 68, 139, 0, 138, 50, 139, 238, 145, 25, 139, 50, 28, 100, 255, 7, 205, 139, 66, 53, 47, 139, 160, 139, 11, 150, 60, 50, 10, 113, 45, 231, 105, 159, 120, 128, 87, 152, 11, 124, 84, 0, 90, 0, 160, 8, 0, 62, 123, 50, 124, 16, 237, 90, 61, 50, 168, 21, 78, 84, 35, 89, 76, 64, 40, 122, 0, 0, 116, 133, 0, 0, 223, 50, 0, 91, 96, 29, 20, 51, 72, 188, 0, 208, 41, 77, 0, 17, 58, 71, 80, 22, 38, 79, 85, 3, 255, 79, 0, 63, 43, 100, 38, 19, 48, 100, 26, 40, 160, 0, 35, 94, 98, 133, 139, 15, 0, 66, 0, 0, 0, 130, 0, 0, 130, 0, 13, 118, 47, 66, 39, 50, 0, 33, 0, 0, 0, 147, 0, 80, 180, 109, 144, 92, 90, 55, 108, 0, 89, 44, 101, 122, 30, 255, 0, 62, 114, 255, 255, 90, 0, 47, 25, 110, 9, 22, 0, 66, 205, 115, 51, 102, 0, 126, 255, 255, 55, 1, 5 };

/* This is used in ReportingGraphs.java which is no longer used. Need to delete this method later */
  // The graph is drawn with this line
  /*public static Color graphLinecolor()
  {
    return new Color(132,66,0);
  }*/

  // Return Color on line index.
  public static Color lineColor(int index)
  {
    // We are taking remainder so that if index is more than number of colors,
    // we can still get color and not cause index out of bound exception.
    int tempIndex = index%(r.length);
    if(userType.equalsIgnoreCase("Business"))
      return new Color(r[tempIndex], g[tempIndex], b[tempIndex]);
    else
      return new Color(r[tempIndex], g[tempIndex], b[tempIndex]);
  }

 // Return Color for compare line.
  public static Color baseLineColor()
  {
     if(userType.equalsIgnoreCase("Business"))
       return new Color(127, 155, 72);
     else
       return new Color(127, 155, 72);
  }

  // Return Color for current test run compare line.
  public static Color currentLineColor()
  {
     if(userType.equalsIgnoreCase("Business"))
       return new Color(64, 105, 157);
     else
       return new Color(64, 105, 157);
  }

  public static Color graphHorizontalinecolor()
  {
     if(userType.equalsIgnoreCase("Business"))
       return new Color(193,165,117);
     else
       return new Color(193,165,117);
  }

  // Captions and Elapsed Time are shown using this method
  public static Color graphCaptionscolor()
  {
    //return new Color(45,0,0);
    if(userType.equalsIgnoreCase("Business"))
      return new Color(255, 0, 0);
    else
      return new Color(255, 0, 0);
  }

  public static Color rightPanelcolor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(216,216,216);
    else
      return new Color(247,251,255);
  }


  public static Color dialGraphcolor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(208,227,250);
    else
      return new Color(247,251,255);
  }

  public static Color leftPanelcolor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(247,251,255);
    else
      return new Color(247,251,255);
  }

  public static Color upperPanelcolor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(255,233,163);
    else
      return new Color(247,251,255);
  }

  //for background color of graph panels
  public static Color graphPanelBGColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(208,227,250);
    else
      return new Color(247,251,255);

  }

  //for upper panel menu bar
  public static Color upperMenuBarColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(0,61,137);
    else
      return new Color(247,251,255);
  }

  //for upper Panel menu bar foreground color
   public static Color upperMenuBarFGColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(255,255,255);
    else
      return new Color(0,0,0);
  }

  //for menu bar
  public static Color menuBarColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(247,251,255);
    else
      return new Color(247,251,255);
  }


  //for lower pane
  public static Color lowerPaneColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(247,251,255);
    else
      return new Color(247,251,255);
  }

  public static Color loginScreenBGColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(247,247,247);
    else
      return new Color(247,247,247);
  }

  public static Color tableHeaderColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(174,174,255);
    else
      return new Color(174,174,255);
  }

  public static Color tableHeaderFgColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return Color.BLACK;
    else
      return Color.BLACK;
  }

  public static Color chartTitleColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(0,61,137);
    else
      return new Color(0,0,0);
  }

  public static Color chartYAxisColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(0,0,0);
    else
      return new Color(0,0,0);
  }

  public static Color linkButtonColor()
  {
     if(userType.equalsIgnoreCase("Business"))
       return new Color(102,102,153);
     else
       return new Color(102,102,153);
  }

  public static Color leftPaneGroupTitleColor()
  {
//    return new Color(128,0,0);  :Done by Atul as per Sanjeev Chopra suggestion
    if(userType.equalsIgnoreCase("Business"))
      return Color.black;
    else
      return Color.black;
  }

  public static Color tableRowSelectionBackGroundColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return Color.LIGHT_GRAY;
    else
      return Color.LIGHT_GRAY;
   //return (new Color(255,192,0));

  }

  public static Color tableRowSelectionForeGroundColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return Color.black;
    else
      return Color.black;
  }

  public static Color leftPaneGraphTitleColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return Color.BLUE;
    else
      return Color.BLUE;
  }


  /*ltableEvenrowcolor and ltableOddrowcolor() are used for alternate shading
  of table rows
  */
  public static Color ltableEvenrowcolor()
  {
    if(userType.equalsIgnoreCase("Business"))
       return new Color(165,165,165);
    else
      return new Color(165,165,165);
  }

  public static Color ltableOddrowcolor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(216,216,216);
    else
      return new Color(216,216,216);
  }

  public static Color ChartBgColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(255,255,255);
    else
      return new Color(255,255,255);
  }


  public static Color buttonBgColor()
  {
//    return new Color(59,89,152);  :Done by Atul as per Sanjeev Chopra suggestion
    if(userType.equalsIgnoreCase("Business"))
      return tableHeaderColor();
    else
      return tableHeaderColor();
  }

  public static Color buttonFgColor()
  {
//    return new Color(255,255,255);  :Done by Atul as per Sanjeev Chopra suggestion
    if(userType.equalsIgnoreCase("Business"))
      return tableHeaderFgColor();
    else
      return tableHeaderFgColor();
  }

  public static Color titleStripColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(7,63,160);
    else
      return new Color(7,63,160);
  }

  public static Color tabelBackGroundColor()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Color(226, 231, 237);
    else
      return new Color(226, 231, 237);
  }

  public static Font smallMicrosoftSansSerifFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Microsoft Sans Serif", Font.BOLD, 9);
    else
      return new Font("Microsoft Sans Serif", Font.BOLD, 9);
  }

  public static Font smallMicrosoftSansSerifPlainFont()
  {
     if(userType.equalsIgnoreCase("Business"))
       return new Font("Microsoft Sans Serif", Font.PLAIN, 9);
     else
       return new Font("Microsoft Sans Serif", Font.PLAIN, 9);
  }

  public static Font mediumMicrosoftSansSerifFont()
  {
    if(userType.equalsIgnoreCase("Business"))
       return new Font("Microsoft Sans Serif", Font.BOLD, 10);
    else
      return new Font("Microsoft Sans Serif", Font.BOLD, 10);
  }


  public static Font mediumMicrosoftSansSerifPlainFont()
  {
     if(userType.equalsIgnoreCase("Business"))
       return new Font("Microsoft Sans Serif", Font.PLAIN, 10);
     else
       return new Font("Microsoft Sans Serif", Font.PLAIN, 10);
  }

  public static Font largeMicrosoftSansSerifFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Microsoft Sans Serif", Font.BOLD, 12);
    else
      return new Font("Microsoft Sans Serif", Font.BOLD, 12);
  }

  public static Font smallFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Verdana", Font.BOLD, 9);
    else
      return new Font("Verdana", Font.BOLD, 9);
  }

  public static Font smallPlainFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Verdana", Font.PLAIN, 9);
    else
      return new Font("Verdana", Font.PLAIN, 9);
  }

  public static Font mediumFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Verdana", Font.BOLD, 10);
    else
      return new Font("Verdana", Font.BOLD, 10);
  }

  public static Font mediumPlainFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Verdana", Font.PLAIN, 10);
    else
      return new Font("Verdana", Font.PLAIN, 10);
  }

  public static Font largeFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Verdana", Font.BOLD, 12);
    else
      return new Font("Verdana", Font.BOLD, 12);
  }

  public static Font menuBarFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Verdana", Font.PLAIN, 12);
    else
      return new Font("Verdana", Font.PLAIN, 12);
  }

  public static Font editorFont()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Verdana", Font.PLAIN, 12);
    else
      return new Font("Verdana", Font.PLAIN, 12);
  }

  // Return Font for Text Box inside a Window
  public static Font labelText()
  {
    if(userType.equalsIgnoreCase("Business"))
      return new Font("Dialog",Font.PLAIN,12);
    else
      return new Font("Dialog",Font.PLAIN,12);
  }

  public static void main(String[] args)
  {
    try
    {
      String colorFileName = "chartColor.ini";
      File colorFileObj = new File(colorFileName);
      FileOutputStream fout = new FileOutputStream(colorFileObj, true);  // Append mode
      PrintStream printStream = new PrintStream(fout);
      String temp = null;
      temp = "TYPE=" + r.length;
      printStream.println(temp);
      //System.out.println("r.length = "+ r.length);
      //System.out.println("g.length = "+ g.length);
      //System.out.println("b.length = "+ b.length);
      //System.out.println("colorName.length = "+ colorName.length);
      for ( int i =1 ; i <= r.length ; i++)
      {
        temp = "#" +  colorName[i-1];
        //System.out.println("temp = "+ temp);
        printStream.println(temp);
        temp = "R" + i + "=" + r[i-1];
        //System.out.println("temp = "+ temp);
        printStream.println(temp);
        temp = "G" + i + "=" + g[i-1];
        //System.out.println("temp = "+ temp);
        printStream.println(temp);
        temp = "B" + i + "=" + b[i-1];
        //System.out.println("temp = "+ temp);
        printStream.println(temp);

      }
      printStream.close();
      fout.close();
    }
    catch(Exception e)
    {
      System.out.println(""+ e);
    }
  }

  public static Color activeNodeColor()
  {
    if(userType.equalsIgnoreCase("Business"))
     return Color.GREEN;
    else
     return Color.GREEN;
  }

  public static Color inActiveNodeColor()
  {
    if(userType.equalsIgnoreCase("Business"))
     return Color.BLACK;
    else
     return Color.BLACK;
  }

  public static Color systemNodeColor()
  {
   if(userType.equalsIgnoreCase("Business"))
     return Color.BLUE;
    else
     return Color.BLUE;
  }
}
