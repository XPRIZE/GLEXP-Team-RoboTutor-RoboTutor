
Story Index Format
-------------------------------------------------------------------------------------------------------------
FYI: I'm building the index and folder structures as follows:  Let me know if you see any problems with this.

{
  "dataSource": [
      {"story":"name of story", "folder": "story1", "level":1, "viewtype":"MARi_Data"},
      {"story":"name of story", "folder": "story2", "level":1, "viewtype":"ASB_Data"},
      {"story":"name of story", "folder": "story3", "level":2, "viewtype":"ASB_Data"},
      {"story":"name of story", "folder": "story4", "level":3, "viewtype":"ASB_Data"}
    ]
}


The actual assets will be in the following folder structure on the Android project Assets folder 

assets/<language ID>/<story id>/

e.g.

assets
    /sw
        /storyindex.json
        /story1
            /storydata.json
            /image1.png
            /image2.png
            /image3.png
        /story2
            /storydata.json
            /image1.png
            /...

    /en
        /storyindex.json
        /story1
            /storydata.json
            /...

    /...





African Story Book data format
-------------------------------------------------------------------------------------------------------------
FYI: Each of these story_data.json files acts as a factory object for a format specific view manager object.

{
    "license":"CC-BY Version 3.0 Unported Licence",

    "story_name":"Ajali mbaya",
    "authors":"Zanele Buthelezi, Thembani Dladla and Clare Verbeek",
    "illustrators":"Rob Owen",
    "language":"Kiswahili",
    "status":"Translated by: Ursula Nafula",
    "copyright":"Copyright(c) School of Education and Development (UKZN) and African Storybook Initiative, 2007",
    "titleimage":"image1.jpg",

    "data": [
      {"text":[["seg1 text", ... , "segN text"], ["seg1 text", ... , "segN text"]], "image":"image1 filename or null"},
      {"text":[["seg1 text", ... , "segN text"]], "image":"image2 filename or null"},
      {etc..}
    ]
}


-------------------------------------------------

The "data" object -

    "data": [
      {"text":[["seg1 text", ... , "segN text"], ["seg1 text", ... , "segN text"]], "image":"image1 filename or null"},
      {"text":[["seg1 text", ... , "segN text"]], "image":"image2 filename or null"},
      {etc..}
    ]

represents a story.

-------------------------------------------------

each line:

      {"text":[["seg1 text", ... , "segN text"], ["seg1 text", ... , "segN text"]], "image":"image1 filename or null"},

represents a page.

-------------------------------------------------

each sub-array:

      ["seg1 text", ... , "segN text"]

represents a paragraph.

-------------------------------------------------

each segment in a sub-array:

      "seg1 text"


 represents  a sentence within that paragraph.



MARi Data Format:
-------------------------------------------------------------------------------------------------------

