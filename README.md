### Twitter client
A basic Android twitter client, built during a @codepath course on app development in Android. The purpose of the assignment was to practice networking with APIs and trying out an ORM for data persistance in addition to using fragments and viewpagers.

#### Details
- How much time did this take?
  - This took a bit more than 25 hours of dev time. Most of that was spent on the UI layouts (including iterations)

#### Requirements
##### Week 2:
- Required user stories completed:
  - [x] All stories from last week complete
  - [x] User can switch between Timeline and Mention views with tabs
  - [x] User can see their own profile
    - [x] Includes avatar, tagline, #/followers, #/following, and a tweet feed
  - [x] User can click on any tweet's avatar to see their profile
  - [x] User can infinitely paginate on any timeline in the app
- Advanced user stories completed:
  - [x] Robust error handling re: network connectivity/other error cases
  - [x] (Done for first week) User can reply to any tweet on timelines
    - [x] (Done for the first week) User can favorite and retweet tweets on timelines
  - [x] Improve the user interface and theme of the app to feel like Twitter
- My own additional features:
  - [x] Viewpager on profile bio area to see description and location like real Twitter app
  - [x] See retweeted tweets the way you would on Twitter ("retweeted by" on the tweet, tweet shows original user's info, etc.)
  - [x] Banner image on profile view like the real Twitter app
  - [x] Swipe down to refresh on any timeline view
 
###### Week 2 demo 
enjoy this demo of the latest features (made with [LICEcap](http://www.cockos.com/licecap/))

![Demo GIF](https://github.com/ekilah/codepathtwitter/blob/master/demo_profile_mentions.gif)

##### Week 1: 
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


###### Week 1 demos
enjoy these demos of week 1's features (made with [LICEcap](http://www.cockos.com/licecap/))

![Demo GIF](https://github.com/ekilah/codepathtwitter/blob/master/demo.gif)

![Demo 2 GIF](https://github.com/ekilah/codepathtwitter/blob/master/demo2.gif)
