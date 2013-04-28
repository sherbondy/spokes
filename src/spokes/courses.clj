(ns spokes.courses)

;; idea: have little photo of our headshot in the corner
;; of the description so that you know who's teaching what.
;; although videos for each course would make this unnecessary.
;; I think we should use sublime video or vimeo for the video player

(def courses
  [
   {:title "The Algorithmic Beauty of Plants"
    :description 
    (str
      "Do you like computers, plants, or art? "
      "How about the intersection of all three?\n\n"
     
      "In this course, we explore the recursive structure of plants "
      "and learn how to make pretty pictures of trees, flowers, and "
      "abstract fractal-like patterns using a clever technique called "
      "L-systems. Everyone will have a chance to create their own "
      "computer-generated works of art inspired by life.")
    :image "/img/courses/abop.jpg"},

   {:title "EyeWire: A game to map the brain"
    :description
    (str
     "EyeWire is a puzzle-meets-coloring book online game that enables "
     "its players to contribute to the brain mapping initiative, which was "
     "announced by President Obama in March. Developed in part by one "
     "of the Spokes teachers in the Seung Lab at MIT, the game teaches "
     "its players how to trace the \"branches\" of neurons through 3D "
     "reconstructions of brain tissue. To do this task, players \"spot check\" "
     "computer algorithms, with the ultimate goal of obtaining a connectivity "
     "map with synaptic-scale resolution of the \"connectome.\"\n\n"
     
     "In the class, everyone will have a chance to build part of a "
     "never-before-been-seen neuron, and will get to build part of the "
     "brain in LEGOs! For more information, check out: "
     "[EyeWire.org](http://eyewire.org) and the "
     "[Connectome TEDTalk](http://www.ted.com/talks/sebastian_seung.html)")
    :image "/img/courses/eyewire.jpg"}
   
   {:title "The Heliostat"
    :description 
    (str
      "Through assembling the Helios heliostat, students will "
      "learn the following fundamental engineering skills: \n\n"
     
      "* __Mechanical Engineering__: They will learn how to build "
        "mechanical assembly's, the importance of tensioning and "
        "aligning belts in a drive train, how to create and use a "
        "press fit, how to couple servo motors to spur gears, and "
        "how to use foam core as a structural material.\n"
      "* __Computer Science__: They will learn how to implement a "
        "microcontroller (Arduino), and the basics of editing an "
        "Arduino program.\n"
      "* __Electrical Engineering__: They will learn how to solder "
        "components together.")
    :image "/img/courses/heliostat.jpg"},

   {:title "How to Build Tasty Foods: Inside, for free!"
    :description
    (str
      "Don't you wish you know how to make your own delicious food? " 
      "With a few old plastic gutters, a handful of plant seeds and a "
      "bit of daily care, you will grow your own veggies in no-time! "
      "Add in a few quick and easy recipes, and you will be the most "
      "popular chef in your high school - period.")
    :image "/img/courses/garden.jpg"},

   {:title "The Science of Music",
    :description
    (str
      "Music has been called the universal language. "
      "In some sense its building blocks of rhythm, harmony, "
      "and melody arise from the nature of the human mind.  "
      "But there are still a lot of unanswered questions!  "
      "Come learn about the math, physics, and psychology "
      "behind the music we love and how to take a scientific "
      "approach to solving its mysteries."),
    :image "/img/courses/music.jpg"},
   
   {:title "Pursuing your Passion"
    :description
    (str
     "After having heard about neuroscience, computer science, "
     "mechanical engineering, music, and more, we hope that your "
     "brain will be buzzing with cool projects you could work on. "
     "The best way to learn, after all, is by doing something you love.\n\n"
     
     "Over the course of a few hours, we'll go over three key steps "
     "to starting your project:\n"

     "1. __Brainstorming__: How do you come up with fun ideas and make "
       "them even better?\n"
     "2. __Communicating__: How do you share your excitement with "
       "your friends?\n"
     "3. __Breaking It Down__: How do you take a huge idea and break "
       "it down into easy-to-handle steps?")
    :image "/img/courses/mountains.jpg"}
   ])
