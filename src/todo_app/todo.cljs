(ns todo-app.todo
  (:require [reagent.core :as r]))

(defonce todos (r/atom (sorted-map)))
(defonce counter (r/atom 0))

(defn delete-todo [id]
  (swap! todos dissoc id)
  (-> (.fetch js/window "http://localhost/todos" (clj->js {:method "DELETE"})) 
      (.then #(js/console.dir %)) 
      (.catch "hello")))

(defn update-todo [id key val]
  (if (and (= key :text) (empty? val))
    (delete-todo id)
    (do (swap! todos assoc-in [id key] val)
        (-> (.fetch js/window "http://localhost/todos" (clj->js {:method "PUT"}))
            (.then #(js/console.dir %))
            (.catch "hello")))))

(defn add-todo [text]
  (when (not-empty text)
    (let [id (swap! counter inc)]
      (swap! todos assoc id {:id id :text text :done false :note "" :due-date ""})
      (-> (.fetch js/window "http://localhost/todos" (clj->js {:method "POST"}))
          (.then #(js/console.dir %))
          (.catch "hello")))))

(defn show-note [id note edit-note show-note-input]
  [:div.btn-container
   [:button {:class "act-btn note"
             :on-click (fn [_] (swap! show-note-input not))} 
    [:i.far.fa-sticky-note]]
   (when @show-note-input
     [:form.td-itm-note-form
      [:textarea {:class "note-txt-area"
                  :value @edit-note 
                  :form "note-input" 
                  :on-change #(reset! edit-note (-> % .-target .-value))}]
      [:div.td-itm-note-act-btns
       [:button {:class "dialog-btn ok"
                 :on-click (fn [e]
                             (.preventDefault e)
                             (update-todo id :note @edit-note)
                             (reset! show-note-input false))} 
        [:i.fas.fa-check]]
       [:button {:class "dialog-btn cancel"
                 :on-click (fn [e] 
                             (.preventDefault e) 
                             (reset! edit-note note)
                             (reset! show-note-input false))} 
        [:i.fas.fa-times]]]])])

(defn show-date [id due-date edit-date show-date-input]
  [:div.btn-container
   [:button {:class "act-btn date" :on-click (fn [_] (swap! show-date-input not))}
    [:i.far.fa-clock]
    (when (not-empty due-date) [:span.td-itm-due-date due-date])]
   (when @show-date-input
     [:form.td-itm-date-form
      [:input {:class "td-date-input"
               :type "date"
               :value @edit-date
               :on-change #(reset! edit-date (-> % .-target .-value))}]
      [:button {:class "dialog-btn ok"
                :on-click (fn [e]
                            (.preventDefault e)
                            (update-todo id :due-date @edit-date)
                            (reset! show-date-input false))} 
       [:i.fas.fa-check]]
      [:button {:class "dialog-btn cancel"
                :on-click (fn [e]
                            (.preventDefault e)
                            (reset! edit-date due-date)
                            (reset! show-date-input false))}
       [:i.fas.fa-times]]])])

(defn show-todo-edit [id is-editing edit-input]
  [:div {:class "td-itm-container" :id (.toString id)}
   [:form.td-itm-edit-form
    [:input {:class "td-itm-edit-input"
             :type "text" :value @edit-input 
             :on-change #(reset! edit-input (-> % .-target .-value))}]
    [:div.td-itm-edit-actions
     [:button {:class "td-itm-editing-act-btn ok"
               :on-click (fn [e]
                           (.preventDefault e)
                           (update-todo id :text @edit-input)
                           (reset! is-editing false))} 
      [:i.fas.fa-check]]
     [:button {:class "td-itm-editing-act-btn cancel"
               :on-click (fn [e]
                           (.preventDefault e)
                           (reset! is-editing false))} 
      [:i.fas.fa-times]]]]])

(defn show-todo-item [id text done note due-date is-editing show-date-input show-note-input edit-date edit-note]
  [:div {:class "todo-item td-itm-container" :id (.toString id)}
   [:div.td-itm-check {:on-click (fn [_] (update-todo id :done (not done)))}
    (if done [:i.far.fa-check-circle.done] [:i.far.fa-circle.not-done])]
   [:span.td-itm-text text]
   [:div.td-itm-actions
    [show-date id due-date edit-date show-date-input]
    [show-note id note edit-note show-note-input]
    [:button.act-btn.edit {:on-click (fn [_] (reset! is-editing true))} [:i.far.fa-edit]]
    [:button.act-btn.delete {:on-click (fn [_] (delete-todo id))} [:i.far.fa-trash-alt]]]])

(defn todo-item [{:keys [id text done note due-date]}]
  (let [is-editing (r/atom false) edit-input (r/atom text) edit-date (r/atom due-date)
        show-date-input (r/atom false) show-notes-input (r/atom false) edit-note (r/atom note)]
    (fn [{:keys [id text done note due-date]}]
       (when (not-empty text)
         (if-not @is-editing
           [show-todo-item id text done note due-date is-editing show-date-input show-notes-input edit-date edit-note]
           [show-todo-edit id is-editing edit-input])))))

(defn todo-input []
  (let [input (r/atom "")]
    (fn []
      [:form.input-form
       [:input.todo-input {:type "text" :value @input :on-change #(reset! input (-> % .-target .-value))}]
       [:button.add-btn {:on-click (fn [e] (.preventDefault e) (add-todo @input) (reset! input ""))} [:i.fas.fa-plus {:style {:margin-right 10}}]  "ADD"]])))

(defn todo-app []
  [:div.app
   [:h1#title "Todo App"]
   [todo-input]
   (for [todo (vals @todos)] ^{:key (:id todo)} [todo-item todo])])

(defn mount-root []
  (r/render [todo-app] (.getElementById js/document "root")))


; (-> (.fetch js/window "http://localhost" (clj->js {:method "GET"})) (.then #(js/console.dir %)) (.catch "hello"))


; (go
;   (let [response (<! (http/get "http://localhost/index.html"
;                                {:with-credentials? false
;                                 :headers {"Access-Control-Allow-Origin" "http://localhost"}}))]
;     (println (:status response))
;     (println "hello")))

; (defn make-remote-call [endpoint]
;   (go (let [response (<! (http/get endpoint
;                                    {:with-credentials? false
;                                     :headers {"Access-Control-Allow-Origin" "*"}}))]
;         (js/console.log (clj->js response))
;         (println response))))

; (make-remote-call "http://localhost/index.html")

