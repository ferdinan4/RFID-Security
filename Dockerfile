FROM ferdinan4/RFID-Security:latest

RUN git clone https://github.com/ferdinan4/RFID-Security.git /ferdinan4/RFID-Security.git
WORKDIR /ferdinan4/RFID-Security

RUN npm install

CMD ["make","serve"]