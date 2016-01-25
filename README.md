# Migrator :Tool to migrate struts web application into a spring web application
 
# Initial Flow
 
# Step 1: FrameworkMigrator.java run this class, which will open a GUI to take input values like input struts project directory path, new migrated spring project name, output directory path for spring project
 

# Step 2: FrameworkMigrator. Process method will basically create initial output project directory structure, as for typical dynamic web project as required. 

# Step3: SourceMigrateAction. processSourceFiles will do migration of java classes

# Step 4: SourceMigrateAction. processWebContent will do migration of config files, jsps