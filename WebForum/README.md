# WebForum
##Endpoints
###Forum ineraction edpoints
GET: `forum/getAllMessages`
accepts:
```
{
    "mainTheme":"xiqgqntcaasbjl",
    "subTheme":"luauamowbh",
    "lastSeenTime":null
}
```
returns:
```
[
    {
        "userName": "etzrdkmdlqzqygdvaotimraufihzoxl",
        "message": "sogtstfxjjajetnqbjdhqkriw",
        "dateTime": "2021-12-11T16:16:22.002694"
    },
    {
        "userName": "zeveeeckgoduqkrdezspqoiznzdmgvz",
        "message": "gfqltwsziutrnzfifxrxokpk",
        "dateTime": "2021-12-11T16:16:22.002907"
    },
    {
        "userName": "obynlwmpgubqzdsissumdkfy",
        "message": "wlzocsrdphbayeridmif",
        "dateTime": "2021-12-11T16:16:22.003039"
    },
    {
        "userName": "ybfuplyoiduwujcogfulppvljdzpn",
        "message": "bhetn",
        "dateTime": "2021-12-11T16:16:22.003138"
    },
    {
        "userName": "scvgutermhypyleesgylybrxidektyznlj",
        "message": "joqimaxxtjrhbzsliloxyiakpet",
        "dateTime": "2021-12-11T16:16:22.003295"
    },
    {
        "userName": "twlimoayhdezdxxsbmkewj",
        "message": "esxkcvodfacldguzxb",
        "dateTime": "2021-12-11T16:16:22.00341"
    }
    ....
]
```
GET: `forum/getAllThemes` 
returns
```
[
    {
        "name": "clkzixklhskfdw",
        "subThemeList": [
            {
                "name": "hjacpkszhsqzebcdpknormyczsj"
            },
            {
                "name": "bahfvgghvsiigqbafixwoeu"
            },
            ...
        ]
    },
    {
        "name": "oegzeockyz",
        "subThemeList": [
            {
                "name": "oqffwrzmsrffubovawloxna"
            },
            {
                "name": "gczcjyzikevqeivogpqpgnnyefsb"
            }
            ...
        ]
    }
    ...
]
```
GET: `forum/getNewMessages`
accepts:
```
{
    "mainTheme":"xiqgqntcaasbjl",
    "subTheme":"luauamowbh",
    "lastSeenTime":2021-12-11T15:27:22
}
```
returns:
```
[
    {
        "userName": "etzrdkmdlqzqygdvaotimraufihzoxl",
        "message": "sogtstfxjjajetnqbjdhqkriw",
        "dateTime": "2021-12-11T16:16:22.002694"
    },
    {
        "userName": "zeveeeckgoduqkrdezspqoiznzdmgvz",
        "message": "gfqltwsziutrnzfifxrxokpk",
        "dateTime": "2021-12-11T16:16:22.002907"
    },
    {
        "userName": "obynlwmpgubqzdsissumdkfy",
        "message": "wlzocsrdphbayeridmif",
        "dateTime": "2021-12-11T16:16:22.003039"
    },
    {
        "userName": "ybfuplyoiduwujcogfulppvljdzpn",
        "message": "bhetn",
        "dateTime": "2021-12-11T16:16:22.003138"
    }
]
 ```
GET: `forum/allUsers`
returns:
```
[
    {
        "userName": "user0"
    },
    {
        "userName": "user1"
    },
    {
        "userName": "user2"
    }
]
```
`forum/activeUsers`
returns:
```
[
    {
        "userName": "user0"
    },
    {
        "userName": "user1"
    },
    {
        "userName": "user2"
    }
]
```

###User endoints
POST: `/user/sendMessage`
accepts:
```
{
    "userName":"user1",
    "message":"message",
    "mainThemeName":"ptmgnadoky",
    "subThemeName":"robuyvmstddjzstqunggymdiuvj"
}
```
returns:
```
{
    "userName": "user1",
    "message": "message",
    "dateTime": "2021-12-11T16:33:39.169939"
}
```