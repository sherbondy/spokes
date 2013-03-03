(ns spokes.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [org.httpkit.server :as http]
            [spokes.views :refer [home route]]))

(def team
  [{:name "Bruno Faviero"
    :bio (str "I am a sophomore at MIT, but a lifelong underclassman. "
              "I love learning new things: writing techniques, sailing strategies, "
              "Ultimate throws, design concepts — anything interesting! I also love teaching, "
              "and currently tutor Algorithms and TA a public speaking class. Lately, "
              "I've enjoyed learning about computer science (my major) and design — two subjects "
              "I am very passionate about! I am excited about Spokes because I want to help get "
              "kids motivated to learn and do things, because I believe there is so much unlocked "
              "potential in everyone that could be easily brought out with engaging ..."
              "I was born in Brazil, moved to Florida at 7, and moved to MIT at 18. "
              "I'm currently a News Editor at *The Tech* and Director of *StartLabs*. "
              "In the spare time I don't have, I enjoy sailing, running, scuba diving, photographing, and other sundry activities.")
    :school "MIT"
    :grad-year 2015}
   {:name "Claire O'Connell"
    :bio (str "Claire is a graduating junior at MIT studying Brain & Cognitive Science. "
              "She was born in England, lived in France until the age of 8 when she moved "
              "to Palo Alto, CA, where she lived until leaving for MIT . She is a dual citizen "
              "of the UK and Canada, and now calls her \"home-base\" Shanghai, China, where "
              "her parents moved in 2010. She is currently working as the director of educational "
              "outreach at EyeWire, a citizen science initiative that she cofounded during January of 2011. "
              "In her spare time, she loves running with her teammates on the MIT cross country "
              "and track teams, competing in the 3k steeplechase, and traveling absolutely anywhere! "
              "She believes that there is a profound connection between education and cognition, "
              "and aims to spend her life discovering that connection and using it to change global education. "
              "And she loves smiley faces. A lot. :)")
    :school "MIT"
    :grad-year 2013}
   {:name "Ethan Sherbondy"
    :bio ""
    :school "MIT"
    :grad-year 2014}
   {:name "Jeff Prouty"
    :bio ""
    :school "MIT"
    :grad-year 2014}
   {:name "Manny Singh"
    :bio ""
    :school "MIT"
    :grad-year 2016}
   {:name "Nathan Kit Kennedy"
    :bio ""
    :school "UC Berkeley"
    :grad-year 2014}
   {:name "Phillip Daniel"
    :bio (str "I am a senior in the Mechanical Engineering department at MIT, "
		    			"with aspirations of improving the world. I was raised in Durham, "
              "North Carolina and I enjoy building things, learning about "
              "entrepreneurship in Boston's entrepreneurial ecosystem, biking, "
              "and playing my trombone. In the short term, I plan to attend graduate "
              "school to study design and product development. Down the road, "
              "I plan to start a company and give entrepreneurship a shot. "
              "If things don't work out there, I will find a job at a product "
              "design firm and live like a normal person, sleeping 8 hours every day.")
    :school "MIT"
    :grad-year 2013}
   {:name "Turner Bohlen"
    :bio (str "I am a junior at MIT majoring in Physics, and pursuing interests in "
              "computer science and cognitive science. I have lived in towns all along "
              "cthe east coast, but think of myself as having grown up in Southborough, MA. "
              "Now, Washington, DC, where my parents recently moved, is home. I have had "
              "more hobbies and interests than I can count, but have more recently focused "
              "on entrepreneurship, innovation, and education. I directed the wonderful "
              "StartLabs team, which is dedicated to providing MIT engineers and scientists "
              "with the resources necessary to start world-changing companies, for the last year, "
              "during which we did some fantastic work interweaving the Boston and MIT communitees. "
              "I am currently working with three others to develop a cheap 3D scanning technology, "
              "and am searching hard for a way to teach every person worldwide that they, too, "
              "can find a passion (or three). You can see a little bit more about me at "
              "[turnerbohlen.com](http://www.turnerbohlen.com).")
    :school "MIT"
    :grad-year 2014}])

(defroutes app-routes
  (GET "/" [] (home team))
  (GET "/route" [] (route))
  (route/resources "/")
  (route/not-found "Not Found"))

;; get rid of wrap-reload in production
(def app
  (-> (handler/site app-routes)))


(defn start [port]
  (http/run-server app {:port (or port 8000)}))

(defn -main
  ([] (-main 8000))
  ([port]
     (let [port (or (env :port) port)]
       (start (cond 
               (string? port) (Integer/parseInt port)
               :else port)))))

;; For interactive development, evaluate these:
(comment)
  (do
    (require '[ring.middleware.reload :as reload])
    (def app (-> app (reload/wrap-reload)))
    (defonce server (start 8000)))
  
;; server returns a function that, when evaluated, stops the server:
;; (server)
