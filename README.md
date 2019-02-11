# Meeting manager

[![asciicast](https://asciinema.org/a/EDJUUaN6ZPeh6StQrHYPtLD15.svg)](https://asciinema.org/a/EDJUUaN6ZPeh6StQrHYPtLD15)

This is one of my lab at the university (my 5th lab in programming). But I think it was interesting so I extended it
a little bit more and now it is looks like a pet project to me.

## The goal
My task is to create a command line app that must use 1 java's `Collection` to manage 1 of the classes I have done in the
forth lab. My class's name is `Meeting` so hence the name of the repo.

The app must let's user type some commands with or without 1 arguments, and it maybe a *multiple line* json string.

The app must also store the user data into files. My variant is to store it into CSV files.

## Features
- For the storing user's data, I wrote my own CSV reader and writer. The reader use the same technique with 
[my json parson in Python](https://github.com/quangloc99/simple-json-parser),

- In order to read JSON object multiple lines and also combine it with another data type, I use
[Google's Gson](https://github.com/google/gson), but because that library was designed to parse String and Files, not
for the command lines, I played around with reflection to bend it to fit my need.

- Also with reflection and annotations, I was able to create a system for my commands very easily. I was able to handle 
commands with more arguments and more types.

## Final thought
I spend a lots of times in this one. Hope I got good point on the defense part :smile:.
