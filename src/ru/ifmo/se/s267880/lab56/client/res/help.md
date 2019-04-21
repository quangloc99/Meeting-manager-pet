# Help
Use command `help` to display this message.
Use command `list-commands` for the full list of commands.
	
# Argument formats
## MeetingJson

	{
		"name"    : String,                // The name of the meeting (required)
		"time"    : DateJson,              // The meeting's time (Default: current time)
		"duration": int,                   // The meeting duration, in minute (Default: 60)
		"location": LocationJson,          // The meeting's location (Default: the 1-st floor of the 1-st building [1, 1])
	}
	
## DateJson
DateJson can have 1 of 3 following forms:
1) `[int, int, int, int, int, int]` - from left to right: year, month, date, hour, minute, second
2) Object representation:


	{
		"year": int,
		"month": int,
		"date": int,
		"hour: int,
		"minute": int,
		"second": int
	}
	
In the 1-st and 3-rd form, if a field is missing, it will be filled with zero or by the current time's values
	
## LocationJson
Right now this app supports very simple location. A location consists of only 2 value:
the building number and the floor number that the meeting will be held.
LocationJson can have 1 of 2 forms:
1) `[int, int]` - from left to right is the building number and then the floor number.
2) Object representation:


	{
		"building": int,                   // Default value is 1
		"floor"   : int,                   // Default value is 1
	}
