# App-Demo-of-COVID-19-Recognition-Model
This project demonstrates how to convert the ResNet to TorchScript and how to use the scripted model in an Android app to perform speech recognition.

## Establishing ResNet Model on Breathing, Cough, Speech Voice Recordings
Please refer to AutoSpeech and COVID-19_DataHackathon_Sensor_Informatics_Challenge for data preprocess and model training.
See https://github.com/VITA-Group/AutoSpeech and https://github.com/huinli/COVID-19_DataHackathon_Sensor_Informatics_Challenge

## Converting the PyTorch Model to TorchScript
Please execute `pth2ptl.py` in `COVID-19_DataHackathon_Sensor_Informatics_Challenge/fusion_resnet/repos/auto` to convert the breathing, cough, speech, and ffn model in `checkpoint` to TorchScript (ends with .ptl in this project). After acquiring the TorchScript, you should put the models under `App-Demo-of-COVID-19-Recognition-Model/app/src/main/assets` folder (if no `assets` folder then create one).

## Android App Development
Completed the function of permission request, voice recording, data preprocessing, and module loading in the `App-Demo-of-COVID-19-Recognition-Model/app/src/main/java/com/example/mlseriesdemo/audio/AudioClassificationActivity.java`.
