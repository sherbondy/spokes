# Spokes

This is the website for our bicycling trip. I strive to document it and make it as self-documenting as possible.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.
For stylesheets, we're using SASS with [sass-twitter-bootstrap][2]
and [Compass][3].

Make sure to do `git submodule update --init` when you first clone
the repository to grab all dependencies.

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/jlong/sass-twitter-bootstrap
[3]: http://compass-style.org

## Running

To start a web server for the application, run:

    lein ring server

To play around, do:

    lein repl
    
Do `rake watch` from `resources/bootstrap` while editing 
the `.scss` files to auto-compile them.

## License

Copyright © 2013 Ethan Sherbondy and the Spokes Team


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
  - SASS (SCSS) for stylesheet sanity.
  - Hiccup for templates. Would be happy to try Enlive too.

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
