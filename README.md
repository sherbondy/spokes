# Spokes

This is the website for our bicycling trip. I strive to document it and make it as self-documenting as possible.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2013 Ethan Sherbondy and the Spokes Team


## Meditations and Contemplations

Degrade nicely.
Even maps should have static representations.
Be responsive: look nice regardless of screen size.

Goals:
  - Inform visitors about the trip and its purpose.
  - Tell them how they can:
    - interact with us (track our progress)
    - donate/sponsor?
    - indicate if we can stay with them
    - polls for towns to visit on the way
    - signups for classes
    - teacher signup


Setup:
  - Sinatra for now. Rails if we start to feel like we're reinventing the wheel.
  - SASS (SCSS) for stylesheet sanity.
  - HAML

Typefaces:
  - A nice cursive script for the logo?
  - Spokes logo: bicycle with p and o as wheels.
  - Google webfonts...

Theme ideas:
  - Mountains, and a road running through the page, with animated bikes zipping by randomly.
  - Sun is dominant on page. Position of sun matches actual time of day sunrise/set preditions based on where we are (Boston until the trip begins...)
  - The WHEEL for "Who", with team members as the spokes :D

