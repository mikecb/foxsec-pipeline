package com.mozilla.secops.httprequest;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mozilla.secops.parser.EventFilter;
import com.mozilla.secops.parser.EventFilterPayload;
import com.mozilla.secops.parser.EventFilterPayloadOr;
import com.mozilla.secops.parser.EventFilterRule;
import com.mozilla.secops.parser.Normalized;

/** Configuration toggles for HTTPRequest pipeline analysis */
@JsonInclude(Include.NON_EMPTY)
public class HTTPRequestToggles {
  // Mode toggles
  private Boolean enableThresholdAnalysis;
  private Boolean enableErrorRateAnalysis;
  private Boolean enableEndpointAbuseAnalysis;
  private Boolean enableHardLimitAnalysis;
  private Boolean enableUserAgentBlacklistAnalysis;
  private Boolean enableNatDetection;

  // Hard limit settings
  private Long hardLimitRequestCount;

  // Threshold analysis settings
  private Double analysisThresholdModifier;
  private Double requiredMinimumAverage;
  private Long requiredMinimumClients;
  private Double clampThresholdMaximum;

  // Error rate settings
  private Long maxClientErrorRate;

  // User agent blacklist settings
  private String userAgentBlacklistPath;

  // Endpoint abuse settings
  private String[] endpointAbusePath;
  private Boolean endpointAbuseExtendedVariance;
  private String[] endpointAbuseCustomVarianceSubstrings;
  private Integer endpointAbuseSuppressRecovery;
  private Long sessionGapDurationMinutes;

  // Filtering settings
  private String[] filterRequestPath;
  private String[] includeUrlHostRegex;
  private String cidrExclusionList;
  private Boolean ignoreCloudProviderRequests;
  private Boolean ignoreInternalRequests;

  // Misc settings
  private String monitoredResource;

  /**
   * Set enable NAT detection setting
   *
   * @param value Boolean
   */
  @JsonProperty("enable_nat_detection")
  public void setEnableNatDetection(Boolean value) {
    enableNatDetection = value;
  }

  /**
   * Get enable NAT detection setting
   *
   * @return Boolean
   */
  public Boolean getEnableNatDetection() {
    return enableNatDetection;
  }

  /**
   * Set threshold analysis setting
   *
   * @param enableThresholdAnalysis Boolean
   */
  @JsonProperty("enable_threshold_analysis")
  public void setEnableThresholdAnalysis(Boolean enableThresholdAnalysis) {
    this.enableThresholdAnalysis = enableThresholdAnalysis;
  }

  /**
   * Get threshold analysis setting
   *
   * @return Boolean
   */
  public Boolean getEnableThresholdAnalysis() {
    return enableThresholdAnalysis;
  }

  /**
   * Set analysis threshold modifier
   *
   * @param value Value
   */
  @JsonProperty("analysis_threshold_modifier")
  public void setAnalysisThresholdModifier(Double value) {
    analysisThresholdModifier = value;
  }

  /**
   * Get analysis threshold modifier
   *
   * @return Double
   */
  public Double getAnalysisThresholdModifier() {
    return analysisThresholdModifier;
  }

  /**
   * Set required minimum average
   *
   * @param value Double
   */
  @JsonProperty("required_minimum_average")
  public void setRequiredMinimumAverage(Double value) {
    requiredMinimumAverage = value;
  }

  /**
   * Get required minimum average
   *
   * @return Double
   */
  public Double getRequiredMinimumAverage() {
    return requiredMinimumAverage;
  }

  /**
   * Set required minimum clients
   *
   * @param value Long
   */
  @JsonProperty("required_minimum_clients")
  public void setRequiredMinimumClients(Long value) {
    requiredMinimumClients = value;
  }

  /**
   * Get required minimum clients
   *
   * @return Long
   */
  public Long getRequiredMinimumClients() {
    return requiredMinimumClients;
  }

  /**
   * Set clamp threshold maximum
   *
   * @param value Double
   */
  @JsonProperty("clamp_threshold_maximum")
  public void setClampThresholdMaximum(Double value) {
    clampThresholdMaximum = value;
  }

  /**
   * Get clamp threshold maximum
   *
   * @return Double
   */
  public Double getClampThresholdMaximum() {
    return clampThresholdMaximum;
  }

  /**
   * Set error rate analysis setting
   *
   * @param enableErrorRateAnalysis Boolean
   */
  @JsonProperty("enable_error_rate_analysis")
  public void setEnableErrorRateAnalysis(Boolean enableErrorRateAnalysis) {
    this.enableErrorRateAnalysis = enableErrorRateAnalysis;
  }

  /**
   * Get error rate analysis setting
   *
   * @return Boolean
   */
  public Boolean getEnableErrorRateAnalysis() {
    return enableErrorRateAnalysis;
  }

  /**
   * Set max client error rate
   *
   * @param value Long
   */
  @JsonProperty("max_client_error_rate")
  public void setMaxClientErrorRate(Long value) {
    maxClientErrorRate = value;
  }

  /**
   * Get max client error rate
   *
   * @return Long
   */
  public Long getMaxClientErrorRate() {
    return maxClientErrorRate;
  }

  /**
   * Set endpoint abuse analysis setting
   *
   * @param enableEndpointAbuseAnalysis Boolean
   */
  @JsonProperty("enable_endpoint_abuse_analysis")
  public void setEnableEndpointAbuseAnalysis(Boolean enableEndpointAbuseAnalysis) {
    this.enableEndpointAbuseAnalysis = enableEndpointAbuseAnalysis;
  }

  /**
   * Get endpoint abuse analysis setting
   *
   * @return Boolean
   */
  public Boolean getEnableEndpointAbuseAnalysis() {
    return enableEndpointAbuseAnalysis;
  }

  /**
   * Set endpoint abuse path
   *
   * @param value String[]
   */
  @JsonProperty("endpoint_abuse_path")
  public void setEndpointAbusePath(String[] value) {
    endpointAbusePath = value;
  }

  /**
   * Get endpoint abuse path
   *
   * @return String[]
   */
  public String[] getEndpointAbusePath() {
    return endpointAbusePath;
  }

  /**
   * Set endpoint abuse extended variance
   *
   * @param value Boolean
   */
  @JsonProperty("endpoint_abuse_extended_variance")
  public void setEndpointAbuseExtendedVariance(Boolean value) {
    endpointAbuseExtendedVariance = value;
  }

  /**
   * Get endpoint abuse extended variance
   *
   * @return Boolean
   */
  public Boolean getEndpointAbuseExtendedVariance() {
    return endpointAbuseExtendedVariance;
  }

  /**
   * Set endpoint abuse custom variance substrings
   *
   * @param value String[]
   */
  @JsonProperty("endpoint_abuse_custom_variance_substrings")
  public void setEndpointAbuseCustomVarianceSubstrings(String[] value) {
    endpointAbuseCustomVarianceSubstrings = value;
  }

  /**
   * Get endpoint abuse custom variance substrings
   *
   * @return String[]
   */
  public String[] getEndpointAbuseCustomVarianceSubstrings() {
    return endpointAbuseCustomVarianceSubstrings;
  }

  /**
   * Set endpoint abuse suppress recovery
   *
   * @param value Integer
   */
  @JsonProperty("endpoint_abuse_suppress_recovery")
  public void setEndpointAbuseSuppressRecovery(Integer value) {
    endpointAbuseSuppressRecovery = value;
  }

  /**
   * Get endpoint abuse suppress recovery
   *
   * @return Integer
   */
  public Integer getEndpointAbuseSuppressRecovery() {
    return endpointAbuseSuppressRecovery;
  }

  /**
   * Set session gap duration minutes
   *
   * @param value Long
   */
  @JsonProperty("session_gap_duration_minutes")
  public void setSessionGapDurationMinutes(Long value) {
    sessionGapDurationMinutes = value;
  }

  /**
   * Get session gap duration minutes
   *
   * @return Long
   */
  public Long getSessionGapDurationMinutes() {
    return sessionGapDurationMinutes;
  }

  /**
   * Set hard limit analysis setting
   *
   * @param enableHardLimitAnalysis Boolean
   */
  @JsonProperty("enable_hard_limit_analysis")
  public void setEnableHardLimitAnalysis(Boolean enableHardLimitAnalysis) {
    this.enableHardLimitAnalysis = enableHardLimitAnalysis;
  }

  /**
   * Get hard limit analysis setting
   *
   * @return Boolean
   */
  public Boolean getEnableHardLimitAnalysis() {
    return enableHardLimitAnalysis;
  }

  /**
   * Set hard limit request count
   *
   * @param value Long
   */
  @JsonProperty("hard_limit_request_count")
  public void setHardLimitRequestCount(Long value) {
    hardLimitRequestCount = value;
  }

  /**
   * Get hard limit request count
   *
   * @return Long
   */
  public Long getHardLimitRequestCount() {
    return hardLimitRequestCount;
  }

  /**
   * Set user agent blacklist analysis setting
   *
   * @param enableUserAgentBlacklistAnalysis Boolean
   */
  @JsonProperty("enable_user_agent_blacklist_analysis")
  public void setEnableUserAgentBlacklistAnalysis(Boolean enableUserAgentBlacklistAnalysis) {
    this.enableUserAgentBlacklistAnalysis = enableUserAgentBlacklistAnalysis;
  }

  /**
   * Get user agent blacklist analysis setting
   *
   * @return Boolean
   */
  public Boolean getEnableUserAgentBlacklistAnalysis() {
    return enableUserAgentBlacklistAnalysis;
  }

  /**
   * Set user agent blacklist path
   *
   * @param value String
   */
  @JsonProperty("user_agent_blacklist_path")
  public void setUserAgentBlacklistPath(String value) {
    userAgentBlacklistPath = value;
  }

  /**
   * Get user agent blacklist path
   *
   * @return String
   */
  public String getUserAgentBlacklistPath() {
    return userAgentBlacklistPath;
  }

  /**
   * Set filter request path
   *
   * @param value String[]
   */
  @JsonProperty("filter_request_path")
  public void setFilterRequestPath(String[] value) {
    filterRequestPath = value;
  }

  /**
   * Get filter request path
   *
   * @return String[]
   */
  public String[] getFilterRequestPath() {
    return filterRequestPath;
  }

  /**
   * Set include URL host regex
   *
   * @param value String[]
   */
  @JsonProperty("include_url_host_regex")
  public void setIncludeUrlHostRegex(String[] value) {
    includeUrlHostRegex = value;
  }

  /**
   * Get include URL host regex
   *
   * @return String[]
   */
  public String[] getIncludeUrlHostRegex() {
    return includeUrlHostRegex;
  }

  /**
   * Set CIDR exclusion list path
   *
   * @param value String
   */
  @JsonProperty("cidr_exclusion_list")
  public void setCidrExclusionList(String value) {
    cidrExclusionList = value;
  }

  /**
   * Get CIDR exclusion list path
   *
   * @return String
   */
  public String getCidrExclusionList() {
    return cidrExclusionList;
  }

  /**
   * Set ignore cloud provider requests
   *
   * @param value Boolean
   */
  @JsonProperty("ignore_cloud_provider_requests")
  public void setIgnoreCloudProviderRequests(Boolean value) {
    ignoreCloudProviderRequests = value;
  }

  /**
   * Get ignore cloud provider requests
   *
   * @return Boolean
   */
  public Boolean getIgnoreCloudProviderRequests() {
    return ignoreCloudProviderRequests;
  }

  /**
   * Set ignore internal requests
   *
   * @param value Boolean
   */
  @JsonProperty("ignore_internal_requests")
  public void setIgnoreInternalRequests(Boolean value) {
    ignoreInternalRequests = value;
  }

  /**
   * Get ignore internal requests
   *
   * @return Boolean
   */
  public Boolean getIgnoreInternalRequests() {
    return ignoreInternalRequests;
  }

  /**
   * Set monitored resource
   *
   * @param value String
   */
  @JsonIgnore
  public void setMonitoredResource(String value) {
    monitoredResource = value;
  }

  /**
   * Get monitored resource
   *
   * @return String
   */
  public String getMonitoredResource() {
    return monitoredResource;
  }

  /**
   * Convert the toggles to a standard EventFilter for use in HTTPRequest
   *
   * <p>Note that this filter does not apply any form of address exclusion if indicated in the
   * toggles. This must be handled outside of the event filter, typically within {@link
   * HTTPRequestElementFilter}.
   *
   * @return EventFilter
   */
  public EventFilter toStandardFilter() {
    EventFilter ret = new EventFilter().passConfigurationTicks().setWantUTC(true);
    EventFilterRule rule = new EventFilterRule().wantNormalizedType(Normalized.Type.HTTP_REQUEST);
    if (filterRequestPath != null) {
      for (String s : filterRequestPath) {
        String[] parts = s.split(":");
        if (parts.length != 2) {
          throw new IllegalArgumentException(
              "invalid format for filter path, must be <method>:<path>");
        }
        rule.except(
            new EventFilterRule()
                .wantNormalizedType(Normalized.Type.HTTP_REQUEST)
                .addPayloadFilter(
                    new EventFilterPayload()
                        .withStringMatch(
                            EventFilterPayload.StringProperty.NORMALIZED_REQUESTMETHOD, parts[0])
                        .withStringMatch(
                            EventFilterPayload.StringProperty.NORMALIZED_URLREQUESTPATH, parts[1]))
                .addPayloadFilter(
                    new EventFilterPayloadOr()
                        .addPayloadFilter(
                            new EventFilterPayload()
                                .withIntegerRangeMatch(
                                    EventFilterPayload.IntegerProperty.NORMALIZED_REQUESTSTATUS,
                                    0,
                                    399))
                        .addPayloadFilter(
                            new EventFilterPayload()
                                .withIntegerRangeMatch(
                                    EventFilterPayload.IntegerProperty.NORMALIZED_REQUESTSTATUS,
                                    500,
                                    Integer.MAX_VALUE))));
      }
    }
    if (includeUrlHostRegex != null) {
      EventFilterPayloadOr orFilter = new EventFilterPayloadOr();
      for (String s : includeUrlHostRegex) {
        orFilter.addPayloadFilter(
            new EventFilterPayload()
                .withStringRegexMatch(
                    EventFilterPayload.StringProperty.NORMALIZED_URLREQUESTHOST, s));
      }
      rule.addPayloadFilter(orFilter);
    }
    ret.addRule(rule);
    return ret;
  }

  /**
   * Initialize {@link HTTPRequestToggles} using {@link HTTPRequest} pipeline options
   *
   * <p>This function exists primarily for conversion of legacy pipeline options to operational
   * pipeline toggles. It is intended to ease migration for invocations of the HTTPRequest pipeline
   * that monitor a single service.
   *
   * @param o Pipeline options
   * @return Initialized toggle configuration
   */
  public static HTTPRequestToggles fromPipelineOptions(HTTPRequest.HTTPRequestOptions o) {
    HTTPRequestToggles ret = new HTTPRequestToggles();

    // Use the monitored resource indicator from pipeline options as the resource name
    // here, since we'd only use this function in cases where a single service is being
    // monitored and we are configuring with global pipeline options.
    //
    // This will ensure the correct service name is used in alert messages.
    ret.setMonitoredResource(o.getMonitoredResourceIndicator());

    ret.setEnableThresholdAnalysis(o.getEnableThresholdAnalysis());
    ret.setEnableErrorRateAnalysis(o.getEnableErrorRateAnalysis());
    ret.setEnableEndpointAbuseAnalysis(o.getEnableEndpointAbuseAnalysis());
    ret.setEnableHardLimitAnalysis(o.getEnableHardLimitAnalysis());
    ret.setEnableUserAgentBlacklistAnalysis(o.getEnableUserAgentBlacklistAnalysis());
    ret.setEnableNatDetection(o.getNatDetection());

    ret.setHardLimitRequestCount(o.getHardLimitRequestCount());

    ret.setAnalysisThresholdModifier(o.getAnalysisThresholdModifier());
    ret.setRequiredMinimumAverage(o.getRequiredMinimumAverage());
    ret.setRequiredMinimumClients(o.getRequiredMinimumClients());
    ret.setClampThresholdMaximum(o.getClampThresholdMaximum());

    ret.setMaxClientErrorRate(o.getMaxClientErrorRate());

    ret.setUserAgentBlacklistPath(o.getUserAgentBlacklistPath());

    ret.setEndpointAbusePath(o.getEndpointAbusePath());
    ret.setEndpointAbuseExtendedVariance(o.getEndpointAbuseExtendedVariance());
    ret.setEndpointAbuseCustomVarianceSubstrings(o.getEndpointAbuseCustomVarianceSubstrings());
    ret.setEndpointAbuseSuppressRecovery(o.getEndpointAbuseSuppressRecovery());
    ret.setSessionGapDurationMinutes(o.getSessionGapDurationMinutes());

    ret.setFilterRequestPath(o.getFilterRequestPath());
    ret.setIncludeUrlHostRegex(o.getIncludeUrlHostRegex());
    ret.setCidrExclusionList(o.getCidrExclusionList());
    ret.setIgnoreCloudProviderRequests(o.getIgnoreCloudProviderRequests());
    ret.setIgnoreInternalRequests(o.getIgnoreInternalRequests());

    return ret;
  }

  /** Initialize new {@link HTTPRequestToggles} with defaults */
  public HTTPRequestToggles() {
    enableThresholdAnalysis = false;
    enableErrorRateAnalysis = false;
    enableEndpointAbuseAnalysis = false;
    enableHardLimitAnalysis = false;
    enableUserAgentBlacklistAnalysis = false;
    enableNatDetection = false;

    hardLimitRequestCount = 100L;

    analysisThresholdModifier = 75.0;
    requiredMinimumAverage = 5.0;
    requiredMinimumClients = 5L;

    maxClientErrorRate = 30L;

    endpointAbuseExtendedVariance = false;
    sessionGapDurationMinutes = 45L;

    ignoreCloudProviderRequests = true;
    ignoreInternalRequests = true;
  }
}
