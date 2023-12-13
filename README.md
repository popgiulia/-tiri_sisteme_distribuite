# Descărcare
1. Folosind linia de comandă se merge în folderul📂 în care se dorește descărcarea proiectului și se execută comanda:
   ```
   git clone https://github.com/bogdan-buie/SDI_Project.git
   ```
2. ⚠️⚠️⚠️**Obligatoriu!!! După ce se face git clone/Update Project se va șterge conținutul fișierului "id.txt"**
# Configurare broker Mosquitto
1. Download Mosquitto: <https://mosquitto.org/download/>
2. Se accesează folderul în care este instalat Mosquitto📂 (ex: "C:\Mosquitto")

3. ✍️În fișierul **mosquitto.conf** se adaugă următoarele linii:
```
listener 1883 
allow_anonymous true
```
- **1883** este portul pe care Mosquitto va asculta mesaje (se poate schimba portul)
- **allow_anonymus** true permite oricăror clienți să se conecteze la broker
	
4. Pentru a  porni broker-ul se accesează, folosind linia de comandă, folder-ul în care este instalat Mosquitto **(ex:"C:\Mosquitto")** și se introduce următoarea comandă :
```mosquitto -v -c mosquitto.conf```

5. Pentru a putea porni 2 instanțe diferite de broker Mosquitto pe aceeași mașină, se mai creează un alt fișier **.conf** (ex: mosquitto2.conf) în care se va specifica **alt port**. Astfel, a doua instață a broker-ului se va porni în felul următor:
```
mosquitto -v -c mosquitto2.conf
```
În acest fel se pot creea mai multe instanțe ale broker-ului pe aceeași mașină💻.

#  Configurare fișier de log-uri
1.  În fișerul de configurare al broker-ului **(ex: mosquitto.conf)** se vor trece următoarele linii
``` 
log_dest file C:\Mosquitto\logs.txt
log_type all
log_timestamp true
log_timestamp_format %Y-%m-%d %H:%M:%S
```
2.  Broker-ul se va porni la fel ca la punctul 5 de la configurarea broker-ului
   
**Atentie!!!** Pentru un alt broker se va trece alta cale pentru fisierul de log-uri în fișierul de configurare asociat acelui broker.
