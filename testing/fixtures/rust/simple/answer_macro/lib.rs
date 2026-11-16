use proc_macro::TokenStream;

#[proc_macro_attribute]
pub fn answer(_attr: TokenStream, _item: TokenStream) -> TokenStream {
    "pub fn answer() -> u32 { 42 }".parse().unwrap()
}
