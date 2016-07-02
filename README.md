# GCDC
Google Calendar Distribution Checker

This project is focused on providing tools for analyze your distribution of specifics events on Google Calendar

# Initialization

1. Create your own app on Google Developer Console with Calendar API
2. Download your credentials
3. Rename your credentials "client_id.json" and place it on resources directory

# Sample

## Code 
<code>
DistributionChecker distributionChecker=new DistributionChecker("your_calendar_name"); <br />
Map<String,Number> map=distributionChecker.getNormalizedDistribution(Arrays.asList(new String[]{"event_1","event_2"});
</code>

## Output 

<code>
{event_1=0.4270833333333333, event_2=0.5729166666666666}
</code>