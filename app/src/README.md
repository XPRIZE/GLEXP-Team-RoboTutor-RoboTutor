
    This folder contains the sourcesets for the RoboTutor Android Studio Project:
    --------------------------------------------------------------------------------

    There are currently 5 folders:

	 -- main				(These 2 folders contain source sets for the project)
	 -- verify				(Verify should contain a copy of EXTERNAL asset data) i.e. from RTAssetPublisher

     -- lib                 (contains JAVA libs to support RTAssetManager.jar)
     -- tutor_xmatrices     (contains the transition matrices and associated support files)
     -- tutors              (contains the individual tutor structural files and data sources)
	 
	 NOTE: verify is not part of the project GIT image.  If must be maintained from the RTAssetPublisher.
	 
	 
    (folder) tutor_matrices:
    -------------------------
    The CSV files found in this folder contain a grid array of tutor descriptors that are processed
    by "Build_Tutor_Transitions.bat" to generate the Activity_Selectors data source. i.e. dev_data.json

    Matrix encoding:
    -----------------
    A matrix descriptor entry is a composition of a tutor variant descriptor along with a datasource descriptor and
    an optional comment.


    There are 2 forms the composition may take:
    -------------------------------------------

    1. [file] encodings.
    ======================

    These "tutorVariants" support [file] encodings
    ----------------------------------------------
    "math", "akira", "bpop.ltr.mix", "bpop.ltr.lc", "bpop.ltr.uc", "bpop.wrd", "bpop.num", "bpop.shp", "write.ltr.uc", "write.ltr.uc.dic", "write.wrd", "write.ltr.lc", "write.ltr.lc.dic", "write.num.dic", "write.num"


    Most tutors use a [file] encoding scheme. With this encoding the tutor descriptor is combined with the datasource descriptor in the following manner.

    <tutor_descr>:<datasource_descr>:<comment>          (here the ":<comment>" portion is optional and does not participate in filename encoding)

    Valid matrix entry for [file] encoding

    e.g. akira:level1
         bpop.ltr.lc:RND_SAY_MC: My comment/description



    **** [file] encoded filenames.
    ================================
    The matrix descriptor uniquely identifies both a tutor and the data source to launch it with.  To eliminate redundancy in the matrix descriptor the datasource filename is formed in the following way:

        From the above examples:

        akira:level1                                      - becomes ->      akira_level1.json
        bpop.ltr.lc:RND_SAY_MC: My comment/description    - becomes ->      bpop.ltr.lc_rnd_say_mc.json

    Notes:
    1. The datasource descriptor may be any string but:
        a: It must produce a unique encoding within the skill/language.
        b. It must be prefixed with the tutor variant descriptor.

    2. The matrix descriptor is not case sensitive - however the filename is and must be lowercase.
    3. The tutor descriptor portion must appear exactly as seen from the list of supported vraiants.
    4. The Matrix Descriptor uses a ':' delimiter while the filename replaces that with an "_".
    5. The comment is discarded in the filename encoding.




    2. [encfolder] encodings.
    ============================

    "tutorVariants" supporting [encfolder] encodings
    ----------------------------------------------------------
    "story.echo", "story.hear", "story.read"


    Tutors with large numbers of datasources use the [encfolder] encoding scheme. With this encoding the tutor descriptor is combined with the datasource descriptor in the following manner.

    Note the double ::

    <tutor_descr>::<datasource_descr>:<comment>          (here the ":<comment>" portion is optional and does not participate in filename encoding)

    Valid matrix entry for [file] encoding

    e.g. story.hear::level1_1
         story.echo::myLevel_contentA: My comment/description


    **** [encfolder] folder structures
    ==========================================
    encfolder encodings discard both the tutor descriptor and the comment when you generate the associated folder structure.

        From the above examples:

        story.hear::level1_1                                - becomes ->      -- level1
        story.hear::level1_2                                - becomes ->            |
                                                                                    |
                                                                                    --- level1_1
                                                                                    |        |
                                                                                    |        --  <files>
                                                                                    |
                                                                                    --- level1_2
                                                                                            |
                                                                                            --  <files>


        story.echo::myLevel_contentA: My comment/description  - becomes ->    -- myLevel
                                                                                    |
                                                                                    -- myLevel_contentA
                                                                                            |
                                                                                            --  <files>

    Notes:
    1. The datasource descriptor may be any string but:
        a: It must produce a unique encoding within the skill/language.
        b. It must be prefixed with the tutor variant descriptor.

    2. The matrix descriptor is not case sensitive - however the folder structure must be and must be lowercase.
    3. The tutor descriptor portion must appear exactly as seen from the list of supported vraiants.
    4. The Matrix Descriptor uses a '::' delimiter while the folder structure looks only at the datasource descriptor.
        a. the datasource descriptor must be in the following format <parent>_<childid>

    5. The comment is discarded in the encfolder encoding.

