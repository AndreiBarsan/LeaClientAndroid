package eu.cs_syd.leaclient;

/**
 * Simple struct populated by Jackson when reading a response from a Lea server.
 */
class LeaResponse {
  public String reply_type;
  public String output_str;
  public String output_delay;
}

