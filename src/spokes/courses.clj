(ns spokes.courses)

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
    :image "/img/courses/abop.jpg"
   },
   
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
    :image "/img/courses/heliostat.jpg"
   },
   ])