require 'sinatra'

get '/' do
  @team = [{name:"Daesun Yim"},
           {name:"Jeff Prouty"},
           {name:"Alisha Lussiez"},
           {name:"Bruno Faviero"},
           {name:"Phillip Daniel"},
           {name:"Cathie Yun"},
           {name:"Ethan Sherbondy"},
           {name:"Turner Bohlen"},
           {name:"Nathan Kit Kennedy"},
           {name:"Claire O'Connell"},
           {name:"Chase Lambert"},
           {name:"Natasha Balwit"},
           {name:"Sophie Geoghan"},
           {name:"Manny Singh"}]

  haml :index
end

