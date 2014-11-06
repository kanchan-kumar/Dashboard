package pac1.Bean.Percentile;

interface PercentileErrorDefinition
{
  StringBuffer pctMessageFileNotFound = new StringBuffer("ERROR: Cannot generate percentile data as percentile raw data file (pctMessage.dat) does not exist.");
  StringBuffer rtgMessageFileNotFound = new StringBuffer("ERROR: Cannot generate percentile data as percentile raw data file (rawMessage.dat) does not exist.");
  StringBuffer pctFullPktNotFound = new StringBuffer("ERROR: Cannot generate percentile data due to corrupted or incomplete file.");
  StringBuffer invalidOptionForTotalRun = new StringBuffer("ERROR: Cannot generate percentile data due to incompatibility of mode type. \n Please convert graphs in whole scenario, then try again.");

  byte PERCENTILE_TOTAL_RUN = 0;
  byte PERCENTILE_RUN_PHASE = 1;
  byte PERCENTILE_SPECIFIED_TIME = 2;
}
