### Twitter client
A basic Android twitter client, built during a @codepath course on app development in Android. The purpose of the assignment was to practice networking with APIs and trying out an ORM for data persistance.

#### Details
- How much time did this take?
  - This took me probably close to 12 hours of dev time. Most of that was spent on the UI layouts (including iterations)
- Required user stories completed:
  - [x] User can sign in to Twitter using OAuth login
  - [x] User can view tweets from their home timeline:
    - [x] User should see username, name, and tweet body for each tweet 
    - [x] Relative timestamp per tweet (i.e.: "8m" for 8 minutes prior to now)
    - [x] User can view more tweets as they scroll (pagination)
  - [x] User can compose a new tweet
    - [x] Compose button on top right (ActionBar/Toolbar)
    - [x] User can then enter a new tweet and post it to Twitter
    - [x] User is taken back to the timeline, and new tweet can be seen
- Optional user stories completed:
  - [x] Links clickable in tweets and open in browser
  - [x] Character count in compose view
- Advanced user stories completed:
  - [x] Offline mode via SQLite storage of old tweets
  - [x] Reply functionality
  - [x] Improve the user interface through styling and coloring (goal: immitate real Twitter UI colors/look)
- Bonus user stories completed:
  - [x] Compose view is in a modal overlay
- My own additional features
  - [x] Retweet functionality
  - [x] Favorite/unfavorite functionality
  - [x] Red character count & disabled tweet button on compose for invalid inputs

####Demo time!
enjoy these demos of the latest features (made with [LICEcap](http://www.cockos.com/licecap/))

![Demo GIF](https://github.com/ekilah/codepathtwitter/blob/master/demo.gif)

![Demo 2 GIF](https://github.com/ekilah/codepathtwitter/blob/master/demo2.gif)
