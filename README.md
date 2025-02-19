# Files and Permissions

This repository contains code examples demonstrating Android Notifications and Settings. See the accompanying [lecture notes](https://info448-s17.github.io/lecture-notes/files-permissions.html) for more details.

# Lecture Note

# Files and Permissions
This lecture discusses how to [working with files](https://developer.android.com/training/basics/data-storage/files.html) in Android. Using the file system allows us to have [persistant data storage](https://developer.android.com/guide/topics/data/data-storage.html) in a more expansive and flexible manner than using the `SharedPreferences` discussed in the previous lecture (and as a supplement to `ContentProvider` databases).

<p class="alert alert-info">This lecture references code found at <https://github.com/info448-s17/lecture10-files-permissions>. <!--Note that this code builds upon the example developed in Lecture 8.--></p>

<p class="alert alert-warning">In order to demonstrate all of the features discussed in this lecture, your device or emulator will need to be running **API 23 (6.0 Marshmallow)** or later.</p>


## File Storage Locations
Android devices split file storage into two types: **Internal storage** and **External storage**. These names come from when devices had built-in memory as well as external SD cards, each of which may have had different interactions. However, with modern systems the "external storage" can refer to a section of a phone's built-in memory as well; the distinctions are instead used for specifying _access_ rather than physical data location.

- [**Internal storage**](https://developer.android.com/guide/topics/data/data-storage.html#filesInternal) is always accessible, and by default files saved internally are _only_ accessible to your app. Similarly, when the user uninstalls your app, the internal files are deleted. This is usually the best place for "private" file data, or files that will only be used by your application.

- [**External storage**](https://developer.android.com/guide/topics/data/data-storage.html#filesExternal) is not always accessible (e.g., if the physical storage is removed), and is usually (but not always) _world-readable_. Normally files stored in External storage persist even if an app is uninstalled, unless certain options are used. This is usually used for "public" files that may be shared between applications.


[When do we use each?](https://developer.android.com/training/basics/data-storage/files.html#InternalVsExternalStorage) Basically, you shuold use _Internal_ storage for "private" files that you don't want to be available outside of the app, and use _External_ storage otherwise.

- Note however that there are publicly-**hidden** _External_ files&mdash;the big distinction between the storage locations is less visibility and more about _access_.

In addition, both of these storage systems also have a **"cache"** location (i.e., an _Internal Cache_ and an _External Cache_). A [cache](https://en.wiktionary.org/wiki/cache) is "(secret) storage for the future", but in computing tends to refer to "temporary storage". The Caches are different from other file storage, in that Android has the ability to automatically delete cached files if storage space is getting low... However, you can't rely on the operating system to do that on its own in an efficient way, so you should still delete your own Cache files when you're done with them! In short, use the Caches for temporary files, and try to keep them _small_ (less than 1MB recommended).

- The user can easily clear an application's cache as well.

In code, using all of these storage locations involve working with the [`File`](https://developer.android.com/reference/java/io/File.html) class. This class represents a "file" (or a "directory") object, and is the same class you may be familiar with from Java SE.

- We can instantiate a `File` by passing it a directory (which is another `File`) and a filename (a `String`). Instantiating the file will create the file on disk (but empty, size 0) if it doesn't already exist.
- We can test if a `File` is a folder with the `.isDirectory()` method, and create new directories by taking a `File` and calling `.mkdir()` on it. We can get a list of `Files` inside the directory with the `listFiles()` method. See more API documentation for more details and options.

The difference between saving files to Internal and External storage, ___in practice___, simply involves which directory you put the file in! This lecture will focus on working with **External storage**, since that code ends up being a kind of "super-set" of implementation details needed for the file system in general. We will indicate what changes need to be made for interacting with Internal storage.

- This lecture will walk through implementing an application that will save whatever the user types into an text field to a file.

Because a device's External storage may be on removable media, in order to interact with it in any way we first need to check whether it is available (e.g., that the SD card is mounted). This can be done with the following [check](https://developer.android.com/guide/topics/data/data-storage.html#MediaAvail) (written as a helper method so it can be reused):

```java
public static boolean isExternalStorageWritable() {
  String state = Environment.getExternalStorageState();
  if (Environment.MEDIA_MOUNTED.equals(state)) {
    return true;
  }
  return false;
}
```

## Permissions {#permissions}
Directly accessing the file system of any computer can be a significant security risk, so there are substantial protections in place to make sure that a malicious app doesn't run roughshod over a user's data. So in order to work with the file system, we first need to discuss how Android handles [permissions](https://developer.android.com/guide/topics/permissions/requesting.html) in more detail.

One of the most import aspect of the Android operating system's design is the idea of <a href="https://en.wikipedia.org/wiki/Sandbox_(computer_security)">**sandboxing**</a>: each application gets its own "sandbox" to play in (where all its toys are kept), but isn't able to go outside the box and play with someone else's toys. The "toys" (components) parts that are outside of the sandbox are things that would be _impactful_ to the user, such as network or file access. Apps are not 100% locked into their sandbox, but we need to do extra work to step outside.

- Sandboxing also occurs at a package level, where packages (applications) are isolated from packages _from other developers_; you can use certificate signing (which occurs as part of our build process automatically) to mark two packages as from the same developer if we want them to interact.

- Additionally, Android's underlying OS is Linux-based, so it actually uses Linux's permission system under the hood (with user and group ids that grant access to particular files or processes).

In order for an app to go outside of its sandbox (and use different components), it needs to request permission to leave. We ask for this permission ("Mother may I?") by declaring out-of-sandbox usages explicitly in the `Manifest`, as we've done before with getting permission to access the Internet or send SMS messages.

Android permissions we can ask for are divided into two categories: [normal and dangerous](https://developer.android.com/guide/topics/permissions/requesting.html#normal-dangerous):

- **Normal permissions** are those that may impact the user (so require permission), but don't pose any serious risk. They are granted by the user at _install time_; if the user chooses to install the app, permission is granted to that app. See [this list](https://developer.android.com/guide/topics/permissions/normal-permissions.html) for examples of normal permissions. `INTERNET` is a normal permission.

- **Dangerous permissions**, on the other hand, have the risk of violating a user's privacy, or otherwise messing with the user's device or other apps. These permissions _also_ need to be granted at install time. But ___IN ADDITION___, starting from Android 6.0 Marshmallow (API 23), users _additionally_ need to grant dangerous permission access **at runtime**, when the app tries to actually invoke the "permitted" dangerous action.

    - The user grants permission via a system-generated pop-up dialog. Note that permissions are granted in "groups", so if the user agrees to give you `RECEIVE_SMS` permission, you get `SEND_SMS` permission as well. See the [list of permission groups](https://developer.android.com/guide/topics/permissions/requesting.html#perm-groups).

    - When the user grant permission at runtime, that permission stays granted as long as the app is installed. But the big caveat is that the user can choose to **revoke** or deny privileges at ___any___ time (they do this though System settings)! Thus you have to check _each time you want to access the feature_ if the user has granted the privileges or not&mdash;you don't know if the user has _currently_ given you permission, even if they had i


Writing to external storage is a _dangerous_ permission, and thus we will need to do extra work to support the Marshmallow runtime permission system.

- In order to support runtime permissions, we need to specify our app's **target SDK** to be `23` or higher AND execute the app on a device running Android 6.0 (Marshmallow) or higher. Runtime permissions are only considered if the OS supports _and_ the app is targeted that high. For lower-API devices or apps, permission is only granted at install time.


First we _still_ need to request permission in the `Manifest`; if we haven't announced that we might ask for permission, we won't be allowed to ask in the future. In particular, saving files to External storage requires `android.permission.WRITE_EXTERNAL_STORAGE` permission (which will also grant us `READ_EXTERNAL_STORAGE` access).

Before we perform a dangerous action, we can check that we currently have permission:

```java
int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.PERMISSION_NAME);
```

- This function basically "looks up" whether we've been granted a particular permission or not. It will return either `PackageManager.PERMISSION_GRANTED` or `PackageManager.PERMISSION_DENIED`.

If permission has been granted, great! We can go about our business (e.g., saving a file to external storage). But if permission has NOT been explicitly granted (at runtime), then we have to ask for it. We do this by calling:

```java
ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.PERMISSION_NAME}, REQUEST_CODE);
```

- This method takes a context and then an _array_ of permissions that we need access to (in case we need more than one). We also provide a request code (an `int`), which we can use to identify that particular request for permission in a callback that will be executed when the user chooses whether to give us access or not. This is the same pattern as when we sent an Intent for a _result_; asking for permission is conceptually like sending an Intent to the permission system!

We can then provide the callback that will be executed when the user decides whether to grant us permission or not:

```java
public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
  switch (requestCode) {
    case REQUEST_CODE:
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //have permission! Do stuff!
      }
    default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }
}
```

We check which request we're hearing the results for, what permissions were granted (if any&mdash;the user can piece-wise grant permissions), and then we can react if everything is good... like by finally saving our file!

- Note that if the user deny us permission once, we might want to try and explain _why_ we're asking permission (see [best practices](https://developer.android.com/training/permissions/best-practices.html)) and ask again. Google offers a utility method (`ActivityCompat#shouldShowRequestPermissionRationale()`) which we can use to show a rationale dialog if they've denied us once. And if that's true, we might show a Dialog or something to explain ourselves--and if they OK that dialog, then we can ask again.


## External Storage
Once we have permission to write to external file, we can actually do so! Since we've verified that the External storage is available, we now need to pick what directory in that storage to save the file in. With External storage, we have two options:

- We can save the file [**publicly**](https://developer.android.com/guide/topics/data/data-storage.html#SavingSharedFiles). We use the `getExternalStoragePublicDirectory()` method to access a public directory, passing in what [type](http://developer.android.com/reference/android/os/Environment.html#lfields) of directory we want (e.g., `DIRECTORY_MUSIC`, `DIRECTORY_PICTURES`, `DIRECTORY_DOWNLOADS` etc). This basically drops files into the same folders that every other app is using, and is great for shared data and common formats like pictures, music, etc.. Files in the public directories can be easily accessed by other apps (assuming the app has permission to read/write from External storage!)

- Alternatively starting from API 18, we save the file [**privately**](https://developer.android.com/guide/topics/data/data-storage.html#AccessingExtFiles), but still on External storage (these files _are_ world-readable, but are hidden from the user as media, so they don't "look" like public files). We access this directory with the `getExternalFilesDir()` method, again passing it a _type_ (since we're basically making our own version of the public folders). We can also use `null` for the type, giving us the root directory.

  Since API 19 (4.4 KitKat), you don't need permission to write to _private_ External storage. So you can specify that you only need permission for versions lower than that:

  ```xml
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18" />
  ```

<p class="alert alert-info">We can actually look at the emulator's file-system and see our files by created using `adb`. Connect to the emulator from the terminal using `adb -s emulator-5554 shell` (note: `adb` needs to be on your PATH). **Public** external files can usually be found in `/storage/sdcard/Folder`, while **private** external files can be found in `/storage/sdcard/Android/data/package.name/files` (these paths may vary on different devices).</p>

Once we've opened up the file, we can write content to it by using the same IO classes we've used in Java:

- The "low-level" way to do this is to create a a `FileOutputStream` object (or a `FileInputStream` for reading). We just pass this constructor the `File` to write to. We write `bytes` to this stream... but can write a String by calling `myString.getBytes()`. For reading, we'll need to read in _all_ the lines/characters, and probably build a String out of them to show. This is actually the same loop we used when reading data from an HTTP request!

- However, we can also use the same _decorators_ as in Java (e.g., `BufferedReader`, `PrintWriter`, etc.) if we want those capabilities; it makes reading and writing to file a little easier

- In either case, **remember to `.close()` the stream when done** (to avoid memory leaks)!

```java
//writing
try {
    //saving in public Documents directory
    File dir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    if (!dir.exists()) { dir.mkdirs(); } //make dir if doesn't otherwise exist
    File file = new File(dir, FILE_NAME);
    Log.v(TAG, "Saving to  " + file.getAbsolutePath());

    PrintWriter out = new PrintWriter(new FileWriter(file, true));
    out.println(textEntry.getText().toString());
    out.close();
} catch (IOException ioe) {
    Log.d(TAG, Log.getStackTraceString(ioe));
}

//reading
try {
    File dir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    File file = new File(dir, FILE_NAME);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuilder text = new StringBuilder();

    //read the file
    String line = reader.readLine();
    while (line != null) {
        text.append(line + "\n");
        line = reader.readLine();
    }

    textDisplay.setText(text.toString());
    reader.close();
} catch (IOException ioe) {
    Log.d(TAG, Log.getStackTraceString(ioe));
}
```

This will allow us to have our "save" button write the message to the file, and have our "read" button load the message from the file (and display it on the screen)!


## Internal Storage & Cache
[Internal storage](https://developer.android.com/guide/topics/data/data-storage.html#filesInternal) works pretty much the same way as External storage. Remember that Internal storage is always _private_ to the app. We also don't need permission to access Internal storage!

For Internal storage, we can use the `getFilesDir()` method to access to the files directory (just like we did with External storage). This method normally returns the folder at `/data/data/package.name/files`.

_Alternatively_, we can use `Context#openFileOutput()` (or `Context#openFileInput()`) and pass it the _name_ of the file to open. This gives us back the `Stream` object for that file in the Internal storage file directory, without us needing to do any extra work (cutting out the middle-man!)

- These methods take a second parameter: `MODE_PRIVATE` will create the file (or _replace_ a file of the same name). Other modes available are: `MODE_APPEND` (which adds to the end of the file if it exists instead of erasing). <s>`MODE_WORLD_READABLE`</s>, and <s>`MODE_WORLD_WRITEABLE`</s> are deprecated.
-  Note that you can wrap a `FileInputStream` in a `InputStreamReader` in a `BufferedReader`.

We can access the Internal Cache directory with `getCacheDir()` (and same read/write process), or the External Cache directory with `getExternalCacheDir()`. We almost always use the Internal Cache, because why would you want temporary files to be world-readable (other than maybe temporary images...)

And again, once you have the file, you use the same process for reading and writing as External storage.

**For practice** make the provided toggle support reading and writing to an Internal file as well. This will of course be  _different_ file than that used with the External switch. Ideally this code could be refactored to avoid duplication, but it gets tricky with the need for checked exception handling.


## Example: Saving Pictures
As another example of how we might use the storage system, consider the "take a selfie" system from [lecture 8](#intents). The code for taking a piecture can be found in a separate `PhotoActivity` (which is accessible via the options menu).

To review: we sent an `Intent` with the `MediaStore.ACTION_IMAGE_CAPTURE` action, and the _result_ of that `Intent` included an _Extra_ that was a `BitMap` of a low-quality thumbnail for the image. But if we want to save a higher resolution version of that picture, we'll need to store that image in the file system!

To do this, we're actually going to modify the `Intent` we _send_ so it includes an additional Extra: a file in which the picture data can be saved. Effectively, we'll have _our Activity_ allocate some memory for the picture, and then tell the Camera where it can put the picture data that it captures. (Intent envelops are too small to carry entire photos around!)

Before we send the `Intent`, we're going to go ahead and create an (empty) file:

```java
File file = null;
try {
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //include timestamp

    //ideally should check for permission here, skipping for time
    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    file = new File(dir, "PIC_"+timestamp+".jpg");
    boolean created = file.createNewFile(); //actually make the file!
    Log.v(TAG, "File created: "+created);

} catch (IOException ioe) {
    Log.d(TAG, Log.getStackTraceString(ioe));
}
```

We will then specify an additional Extra to give that file's location to the camera: if we use `MediaStore.EXTRA_OUTPUT` as our Extra's _key_, the camera will know what to do with that! However, the extra won't actually be the `File` but a **Uri** (recall: the "url" or location of a file). We're not sending the file itself, but the ___location___ of that file (because it's smaller data to fit in the Intent envelope).

- We can get this Uri with the `Uri.fromFile(File)` method:

    ```java
    //save as instance variable to access later when picture comes back
    pictureFileUri = Uri.fromFile(file);
    ```

- Then when we get the picture result back from the Camera (in our `onActivityResult` callback), we can access that file at the saved Uri and use it to display the image! The `ImageView.setImageUri()` is a fast way of showing an image file.

<p class="alert alert-info">Note that when working with images, we can very quickly run out of memory (because images can be huge). So we'll often want to ["scale down"](https://developer.android.com/topic/performance/graphics/index.html) the images as we load them into memory. Additionally, image processing can take a while so we'd like to do it off the main thread (e.g., in an `AsyncTask`). This can become complicated; the recommended solution is to use a third-party library such as [Glide](https://github.com/bumptech/glide), [Picasso](http://square.github.io/picasso/), or [Fresco](http://frescolib.org/).</p>

## Sharing Files
Once we have a file storing the image, we can also save that image with other apps!

As always, in order to interact with other apps, we use an Intent. We can craft an _implicit intent_ for `ACTION_SEND`, sending a message to any apps that are able to send (share) pictures. We'll set the data type as `image/*` to mark this as an image. We will also attach the file as an extra (specifically an `EXTRA_STREAM`). Again note that we don't actually put the _file_ in the extra, but rather tha **Uri** for the file!

Since multiple activities may support this action, we can wrap the intent in a "chooser" to force the user to pick which Activity to use:

```java
Intent chooser = Intent.createChooser(shareIntent, "Share Picture");
//check that there is at least one option
if (shareIntent.resolveActivity(getPackageManager()) != null) {
  startActivity(chooser);
}
```

There is one complication though: because we're saving files in External storage, the app who is executing the `ACTION_SEND` will need to have permission to read the file (e.g., to access External storage). The Messenger app on the emulator appears to lack this permission by default, though we need to take a slightly different approach:

Rather than putting the `file://` Uri in the Intent's extra, we'll need to create a `content://` Uri for a _ContentProvider_ who is able to provide files to anyone who requests them regardless of permissions (the provider grants permission to access its content). Luckily, each image stored in the public directories is automatically tracked by a ContentProvider known as the [`MediaStore`](https://developer.android.com/reference/android/provider/MediaStore.html). It easy to fetch a `content://` Uri for a particular image file from this provider:

```java
MediaScannerConnection.scanFile(this, new String[] {file.toString()}, null,
        new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                mediaStoreUri = uri;  //save the content:// Uri for later
                Log.v(TAG, "MediaStore Uri: "+uri);
            }
        });
```

This provides a Uri that can be given to the Intent, and that the Messenger app will be able to access! We can generate this Uri as soon as we have a file for the image to be saved in.

### Bonus: Sharing with a `FileProvider`

<p class="alert alert-warning">This section has not be edited for formatting or content.</p>

What happens if we try and share an Internal file? You'll get an error (actually notified the use!), because the other (email) app doesn't have permission to read that file!

- There is a way around this though, and it's by using a `ContentProvider` (haha!) A `ContentProvider` explicit is about making content available outside of a package (that's why we declared it in the `Manifest`). Specifically, a `ContentProvider` can convert a set of `Files` into a set of data contents (e.g., accessible with the `content://` protocol) that can be used and returned and understood by other apps!

    - Kind of like a "File Server"

- Android includes a [`FileProvider`](http://developer.android.com/reference/android/support/v4/content/FileProvider.html) class in the support library that does exactly this work.

Setting up a `FileProvider` is luckily not too complex, though it has a couple of steps. You will need to declare the `<provider>` inside you Manifest (see the [guide link](http://developer.android.com/training/secure-file-sharing/setup-sharing.html) for an example).

```xml
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="edu.uw.mapdemo.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/fileprovider" />
</provider>
```

The attributes you will need to specify are:

- `android:authority` should be your package name followed by `.fileprovider` (e.g., `edu.uw.myapp.fileprovider`). This says what source/domain is granting permission for others to use the file.

- The child `<meta-data>` tag includes an `androd:resource` attribute that should point to an XML resource, of type `xml` (the same as used for your SharedPreferences). _You will need to create this file!_ The contents of this file will be a list of what _subdirectories_ you want the `FileProvider` to be able to provide. It will look something like:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <files-path name="my_maps" path="maps/" />
</paths>
```

The `<files-path>` entry refers to a subdirectory inside the Internal Storage files (the same place that `.getFilesDir()` points to), with the `path` specifying the name of the subdirectory (see why we made one called `maps/`?)

Once you have the provider specified, you can use it to get a `Uri` to the "shared" version of the file using:

```java
Uri fileUri = FileProvider.getUriForFile(context, "edu.uw.myapp.fileprovider", fileToShare);
```

(note that the second parameter is the "authority" you specified in your `<provider>` in the Manifest). You can then use this `Uri` as the `EXTRA_STREAM` extra in the Intent that you want to share!
