# Spokes

This is the website for our bicycling trip. I strive to document it and make it as self-documenting as possible.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.
For stylesheets, we're using less with [twitter-bootstrap][2].
Do `bower install` to install the client-side dependencies.

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/twitter/bootstrap

## Running

To start a web server for the application, run:

    lein run

To play around, do:

    lein repl

To reload the site after making changes, do (from inside the repl):
    (refresh-and-restart)

## License

Copyright © 2013 Ethan Sherbondy and the Spokes Team

## Uploading the Site to Amazon

The site is currently hosted statically on S3.
I've been using [s3cmd][3] to sync things.
Here's the command I made up (change ~/code/ to wherever you have the site stored on your computer):

    s3cmd sync --acl-public --recursive --guess-mime-type ~/code/spokes/resources/public/* s3://www.spokesamerica.org/

[3]: http://s3tools.org/s3cmd


## Meditations and Contemplations

Degrade nicely.
Even maps should have static representations.
Be responsive: look nice regardless of screen size.

Goals:
  - Single Page Site, should work well on mobile devices
  - Inform visitors about the trip and its purpose.
  - Tell them how they can:
    - interact with us (track our progress)
    - donate/sponsor?
      - Have Donate button on the page (Stripe/PayPal?)
    - indicate if we can stay with them
    - polls for towns to visit on the way
    - signups for classes
    - teacher signup
    - general contact form for teachers, students, sponsors...


Setup:
  - Simple Compojure server. Could honestly just generate
    static pages for most of the site.
  - Less.js (bootstrap) for stylesheet sanity, see: `/resources/boostrap/less/spokes.less`.
  - To compile the css, do: `make bootstrap` from inside the bootstrap directory. Or [`watch make bootstap`][4] if you're feeling fancy.
  - Hiccup for templates. Would be happy to try Enlive too.

[4]: https://github.com/visionmedia/watch

Typefaces:
  - A nice cursive script for the logo?
  - Spokes logo: bicycle with p and o as wheels.
  - Google webfonts...

Theme ideas:
  - Mountains, and a road running through the page, with animated bikes zipping by randomly.
  - Sun is dominant on page. Position of sun and coloring of sky matches actual time of day sunrise/set preditions based on where we are (Boston until the trip begins...)
  - The WHEEL for "Who", with team members as the spokes :D

Map (the project within the project):
  - Make map start as an image. Then, when the user clicks, it becomes interactive.
  - Show our current location
  - Start with sparsely populated map, but let visitors maximize the page
    to focus on it.
  - Store route offline using LocalStorage API
  - Filter by type of waypoint, e.g. Lodgings, Restrooms, Campgrounds
  - Show nearby waypoints based on current location
  - Filter by section of trail, e.g. Western Express

Sponsors float around in the cloud.
