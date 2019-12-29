function changeArticleSubmit(type) {
    let changeArticleForm = document.form_change_article
    let checks = document.getElementsByName("article_check")
    let article_id = null;

    // check selected elelent
    for (let index = 0; index < checks.length; index++) {
        if (checks[index].checked) {
            article_id = checks[index].getAttribute("data-id")
            break;
        }
    }
    if (article_id == null) {
        alert("記事を選択してください")
        return
    }

    switch (type) {
        case "update":
            changeArticleForm.action = "/edit/" + article_id // request for server
            changeArticleForm.submit()
            break;
        case "delete":
            changeArticleForm.action = "/delete/confirm/" + article_id
            changeArticleForm.submit()
        default:
            break;
    }
}

function scrollToAreaChangeArticle() {
    window.scrollTo({
        top: document.documentElement.scrollHeight - document.documentElement.clientHeight,
        behavior: "smooth"
    });
}