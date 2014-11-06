package pac1.Bean;

public class HarCavCustomFields
{
  public static final String CAV_CACHE_VENDOR = "_cav_cache_provider";
  public static final String BROWSER_CACHE = "browser"; //browser
  public static final String AKAMAI = "akamai"; //akamai
  public static final String STRANGELOOP = "strangeloop"; //strangeloop
  public static final String CLOUDFRONT = "cloudfront"; //CloudFront
  
  
  public static String CAV_CACHE_STATE = "_cav_cache_state"; //HIT, MISS, RHIT
  //All are state
  public static final String HIT = "hit";
  public static final String MISS = "miss";
  public static final String OTHERS = "others";
  public static final String HIT_304 = "hit-304";
  
  public static final String CAV_START_RENDER = "_cav_startRender";
  public static final String CAV_END_RENDER = "_cav_endRender";
}
