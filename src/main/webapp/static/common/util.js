function replaceTurkishCharacters(str) {
    //Convert Characters
    var returnString = str.replace(/ö/g, 'o');
    returnString = returnString.replace(/Ö/g, 'O');
    returnString = returnString.replace(/ç/g, 'c');
    returnString = returnString.replace(/Ç/g, 'c');
    returnString = returnString.replace(/ş/g, 's');
    returnString = returnString.replace(/Ş/g, 'S');
    returnString = returnString.replace(/ı/g, 'i');
    returnString = returnString.replace(/İ/g, 'I');
    returnString = returnString.replace(/ğ/g, 'g');
    returnString = returnString.replace(/Ğ/g, 'G');
    returnString = returnString.replace(/ü/g, 'u');
    returnString = returnString.replace(/Ü/g, 'U');

    return returnString;
}

function printError(err) {
    console.log(err);
}

function isUrl(s) {
    var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
    var regexp2 = /(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
    
    return regexp.test(s) || regexp2.test(s);
}